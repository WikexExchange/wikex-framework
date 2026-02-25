package com.wikex.wikex.p2p.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.p2p.config.CoinExchangeFactory;
import com.wikex.wikex.p2p.entity.Advertise;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.service.AdvertiseService;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.p2p.vo.MemberAdvertiseDetail;
import com.wikex.wikex.p2p.vo.MemberAdvertiseInfo;
import com.wikex.wikex.p2p.vo.ScanAdvertise;
import com.wikex.wikex.screen.AdvertiseScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.feign.CountryFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.feign.PaymentFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.BigDecimalUtils;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;

import static com.wikex.wikex.util.BigDecimalUtils.compare;



@Api(tags = "Advertise")
@RestController
@RequestMapping("/advertise")
@Slf4j
public class AdvertiseController extends BaseController {

    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private MemberFeign memberService;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private CoinExchangeFactory coins;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private PaymentFeign paymentFeign;
    @Autowired
    private CountryFeign countryService;
    @Value("${spark.system.advertise:1}")
    private int allow;


    @ApiOperation(value = "Create Advertise")
    @PermissionOperation
    @RequestMapping(value = "create")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult create(@Valid Advertise advertise, BindingResult bindingResult,
                                @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                @RequestParam(value = "pay[]") String[] pay, String jyPassword) throws Exception {
        AuthMember member = AuthMember.toAuthMember(authMember);
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Assert.notEmpty(pay, msService.getMessage("MISSING_PAY"));
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member1 = memberService.findMemberById(member.getId());
        Assert.isTrue(member1.getIdNumber() != null, msService.getMessage("NO_REALNAME"));

        Assert.isTrue(member1.getMemberLevel().equals(MemberLevelEnum.IDENTIFICATION.getCode()), msService.getMessage("NO_BUSINESS"));

        String mbPassword = member1.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(MD5.md5(jyPassword + member1.getSalt()).toLowerCase().equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        AdvertiseType advertiseType = AdvertiseType.creator(advertise.getAdvertiseType());
        StringBuffer payMode = checkPayMode(pay, advertiseType, member1);
        advertise.setPayMode(payMode.toString());
        OtcCoin otcCoin = otcCoinService.getById(advertise.getCoinId());
        checkAmount(advertiseType, advertise, otcCoin, member1);
        advertise.setCoinUnit(otcCoin.getUnit());
        advertise.setUsername(member1.getUsername());
        advertise.setStatus(AdvertiseControlStatus.PUT_OFF_SHELVES.getCode());
        advertise.setDealAmount(BigDecimal.ZERO);
        advertise.setCreateTime(new Date());
        advertise.setLevel(AdvertiseLevel.ORDINARY.getCode());
        advertise.setRemainAmount(advertise.getNumber());
        advertise.setMemberId(member.getId());
        boolean success = advertiseService.save(advertise);
        if (success) {
            return MessageResult.success(msService.getMessage("CREATE_SUCCESS"));
        } else {
            return MessageResult.error(msService.getMessage("CREATE_FAILED"));
        }
    }


    @ApiOperation(value = "All personal adverts")
    @PermissionOperation
    @RequestMapping(value = "all")
    public MessageResult allNormal(
            PageParam pageParam,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember, HttpServletRequest request) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        LambdaQueryWrapper<Advertise> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Advertise::getMemberId,member.getId());
        queryWrapper.ne(Advertise::getStatus,AdvertiseControlStatus.TURNOFF.getCode());
        if(request.getParameter("status") != null){
            queryWrapper.eq(Advertise::getStatus,AdvertiseControlStatus.valueOf(request.getParameter("status")).getCode());
        }
        queryWrapper.orderByDesc(Advertise::getId);
        Page<Advertise> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        Page<Advertise> all = advertiseService.page(page,queryWrapper);
        return success(IPage2Page(all));
    }


    @ApiOperation(value = "All personal adverts")
    @PermissionOperation
    @RequestMapping(value = "self/all")
    public MessageResult self(
            AdvertiseScreen screen,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        screen.setMemberId(member.getId());
        Page<Advertise> all = advertiseService.findAll(screen);
        return success(IPage2Page(all));
    }


    @ApiOperation(value = "Advertise detail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id")
    })
    @PermissionOperation
    @RequestMapping(value = "detail")
    public MessageResult detail(Long id, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        MemberAdvertiseDetail advertise = advertiseService.findOne(id, member.getId());
        advertise.setMarketPrice(coins.get(advertise.getCoinUnit(),advertise.getCountry().getLocalCurrency()));
        MessageResult result = MessageResult.success();
        result.setData(advertise);
        return result;
    }


    @ApiOperation(value = "Update Advertise")
    @PermissionOperation
    @RequestMapping(value = "update")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult update(
            @Valid Advertise advertise,
            BindingResult bindingResult,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "pay[]") String[] pay, String jyPassword) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        AuthMember shiroUser = AuthMember.toAuthMember(authMember);
        Assert.notEmpty(pay, msService.getMessage("MISSING_PAY"));
        Assert.notNull(advertise.getId(), msService.getMessage("UPDATE_FAILED"));
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));

        Member member = memberService.findMemberById(shiroUser.getId());

        Assert.isTrue(MD5.md5(jyPassword + member.getSalt()).toLowerCase().equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        AdvertiseType advertiseType = AdvertiseType.creator(advertise.getAdvertiseType());

        StringBuffer payMode = checkPayMode(pay, advertiseType, member);

        advertise.setPayMode(payMode.toString());
        Advertise old = advertiseService.getById(advertise.getId());
        Assert.notNull(old, msService.getMessage("UPDATE_FAILED"));
        Assert.isTrue(old.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES.getCode()), msService.getMessage("AFTER_OFF_SHELVES"));
        OtcCoin otcCoin = otcCoinService.getById(old.getCoinId());
        checkAmount( AdvertiseType.creator(old.getAdvertiseType()), advertise, otcCoin, member);
        old.setCountry(advertise.getCountry());
        Advertise ad = advertiseService.modifyAdvertise(advertise, old);
        if (ad != null) {
            return MessageResult.success(msService.getMessage("UPDATE_SUCCESS"));
        } else {
            return MessageResult.error(msService.getMessage("UPDATE_FAILED"));
        }
    }


    @ApiOperation(value = "Put Advertise On Shelves")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id")
    })
    @PermissionOperation
    @RequestMapping(value = "/on/shelves")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult putOnShelves(long id, @RequestHeader(SysConstant.SESSION_MEMBER) String member) throws InformationExpiredException {
        AuthMember authMember = AuthMember.toAuthMember(member);
        Advertise advertise = advertiseService.find(id, authMember.getId());
        Assert.isTrue(advertise != null, msService.getMessage("PUT_ON_SHELVES_FAILED"));
        Assert.isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES.getCode()), msService.getMessage("PUT_ON_SHELVES_FAILED"));
        OtcCoin otcCoin = otcCoinService.getById(advertise.getCoinId());
        if (advertise.getAdvertiseType().equals(AdvertiseType.SELL.getCode())) {
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), authMember.getId());
            Assert.isTrue(compare(memberWallet.getBalance(), advertise.getNumber()), msService.getMessage("INSUFFICIENT_BALANCE"));
            Assert.isTrue(advertise.getNumber().compareTo(otcCoin.getSellMinAmount()) >= 0, msService.getMessage("SELL_NUMBER_MIN") + otcCoin.getSellMinAmount());
            MessageResult result = memberWalletService.freezeBalance(memberWallet.getId(), advertise.getNumber());
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            Assert.isTrue(advertise.getNumber().compareTo(otcCoin.getBuyMinAmount()) >= 0, msService.getMessage("BUY_NUMBER_MIN") + otcCoin.getBuyMinAmount());
        }
        advertise.setRemainAmount(advertise.getNumber());
        advertise.setStatus(AdvertiseControlStatus.PUT_ON_SHELVES.getCode());
        advertiseService.updateById(advertise);
        return MessageResult.success(msService.getMessage("PUT_ON_SHELVES_SUCCESS"));
    }


    @ApiOperation(value = "Put Advertise Off Shelves")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id")
    })
    @PermissionOperation
    @RequestMapping(value = "/off/shelves")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult putOffShelves(long id, @RequestHeader(SysConstant.SESSION_MEMBER) String member) throws InformationExpiredException {
        AuthMember authMember = AuthMember.toAuthMember(member);
        Advertise advertise = advertiseService.find(id, authMember.getId());
        Assert.isTrue(advertise != null, msService.getMessage("PUT_OFF_SHELVES_FAILED"));
        Assert.isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES.getCode()), msService.getMessage("PUT_OFF_SHELVES_FAILED"));
        OtcCoin otcCoin = otcCoinService.getById(advertise.getCoinId());
        if (advertise.getAdvertiseType().equals(AdvertiseType.SELL.getCode())) {
            MessageResult result = memberWalletService.thawBalance(otcCoin.getName(),authMember.getId(), advertise.getRemainAmount());
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        }
        int ret = advertiseService.putOffShelves(advertise.getId(), advertise.getRemainAmount());
        if (!(ret > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        return MessageResult.success(msService.getMessage("PUT_OFF_SHELVES_SUCCESS"));
    }


    @ApiOperation(value = "Delete Advertise")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id")
    })
    @PermissionOperation
    @RequestMapping(value = "delete")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult delete(Long id, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember shiroUser = AuthMember.toAuthMember(authMember);
        Advertise advertise = advertiseService.find(id, shiroUser.getId());
        Assert.notNull(advertise, msService.getMessage("DELETE_ADVERTISE_FAILED"));
        Assert.isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES.getCode()), msService.getMessage("DELETE_AFTER_OFF_SHELVES"));
        advertise.setStatus(AdvertiseControlStatus.TURNOFF.getCode());
        advertiseService.updateById(advertise);
        return MessageResult.success(msService.getMessage("DELETE_ADVERTISE_SUCCESS"));
    }


    @ApiOperation(value = "Query Excellent Advertises")
    @RequestMapping(value = "excellent")
    public MessageResult allExcellentAdvertise(@RequestParam(value = "currency", defaultValue = "CNY") String currency,AdvertiseType advertiseType) throws Exception {
        List<Map<String, String>> marketPrices = new ArrayList<>();
        List<OtcCoin> otcCoins = otcCoinService.getNormalCoin();
        otcCoins.stream().forEachOrdered(x -> {
            Map<String, String> map = new HashMap<>(2);
            map.put("name", x.getUnit());
            map.put("price", coins.get(x.getUnit(),currency).toString());
            marketPrices.add(map);
        });
        List<ScanAdvertise> list = advertiseService.getAllExcellentAdvertise(advertiseType, marketPrices);
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(list);
        return messageResult;
    }


    @ApiOperation(value = "Paginate Advertises")
    @ApiImplicitParams({
    })
    @RequestMapping(value = "page")
    public MessageResult queryPageAdvertise(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "country", defaultValue = "Vietnam") String country,
                                            Long id, AdvertiseType advertiseType,
                                            @RequestParam(value = "isCertified", defaultValue = "0") Integer isCertified){
        OtcCoin otcCoin = otcCoinService.getById(id);
        Country country1 = countryService.findByZhName(country);
        double marketPrice = coins.get(otcCoin.getUnit(),country1.getLocalCurrency()).doubleValue();
        Page<ScanAdvertise> page = advertiseService.paginationAdvertise(pageNo, pageSize,country, otcCoin, advertiseType, marketPrice, isCertified);
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(IPage2Page(page));
        return messageResult;
    }

    @ApiOperation(value = "Paginate Advertises by Unit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "Coin Unit")
    })
    @RequestMapping(value = "page-by-unit")
    public MessageResult queryPageAdvertiseByUnit(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                  @RequestParam(value = "country", defaultValue = "Vietnam") String country,
                                                  String unit, AdvertiseType advertiseType,
                                                  @RequestParam(value = "isCertified", defaultValue = "0") Integer isCertified) {
        OtcCoin otcCoin = otcCoinService.findByUnit(unit);
        Assert.notNull(otcCoin, "validate otcCoin unit!");
        Country country1 = countryService.findByZhName(country);
        BigDecimal price = coins.get(otcCoin.getUnit(), country1.getLocalCurrency());
        double marketPrice = price==null?0:price.doubleValue();
        Page<ScanAdvertise> page = advertiseService.paginationAdvertise(pageNo, pageSize, country,otcCoin, advertiseType, marketPrice, isCertified);
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(IPage2PageOtc(page));
        return messageResult;
    }

    @ApiOperation(value = "Query Advertises by Member Name")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "Member Name")
    })
    @RequestMapping(value = "member", method = RequestMethod.POST)
    public MessageResult memberAdvertises(String name) {
        Member member = memberService.findByUsername(name);
        if (member != null) {
            MemberAdvertiseInfo memberAdvertise = advertiseService.getMemberAdvertise(member, coins.getCoins());
            MessageResult result = MessageResult.success();
            result.setData(memberAdvertise);
            return result;
        } else {
            return MessageResult.error(msService.getMessage("MEMBER_NOT_EXISTS"));
        }
    }

    private StringBuffer checkPayMode(String[] pay, AdvertiseType advertiseType, Member member) {
        List<PaymentTypeRecord> records = paymentFeign.getRecordsByUserId(member.getId());
        if(records==null || records.size()==0){
            throw new IllegalArgumentException("pay parameter error");
        }
        for (PaymentTypeRecord record : records) {
            PaymentType type = paymentFeign.findPaymentTypeById(record.getType());
            record.setTypeName(type.getCode());
        }
        StringBuffer payMode = new StringBuffer();
        Arrays.stream(pay).forEach(x -> {
            if (advertiseType.equals(AdvertiseType.SELL)) {
                boolean isEx = false;
                for (PaymentTypeRecord record : records) {
                    if(record.getTypeName().equals(x)){
                        isEx = true;
                        break;
                    }
                }
                if(!isEx){
                    throw new IllegalArgumentException("pay parameter error");
                }
            }
            payMode.append(x + ",");
        });
        return payMode.deleteCharAt(payMode.length() - 1);
    }

    private void checkAmount(AdvertiseType advertiseType, Advertise advertise, OtcCoin otcCoin, Member member) {
        if (advertiseType.equals(AdvertiseType.SELL)) {
            Assert.isTrue(compare(advertise.getNumber(), otcCoin.getSellMinAmount()), msService.getMessage("SELL_NUMBER_MIN") + otcCoin.getSellMinAmount());
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), member.getId());
            Assert.isTrue(compare(memberWallet.getBalance(), advertise.getNumber()), msService.getMessage("INSUFFICIENT_BALANCE"));
        } else {
            Assert.isTrue(compare(advertise.getNumber(), otcCoin.getBuyMinAmount()), msService.getMessage("BUY_NUMBER_MIN") + otcCoin.getBuyMinAmount());
        }
    }


    @ApiOperation(value = "Query 10 Newest Advertises")
    @RequestMapping(value = "newest")
    public MessageResult queryNewest(@RequestParam(value = "currency", defaultValue = "CNY") String currency) throws Exception {
        List<ScanAdvertise> list = advertiseService.getLatestAdvertise();
        OtcCoin otcCoin;
        double finalPrice;
        if(list==null) {
            return success("data null!");
        }
        for (ScanAdvertise adv : list) {
            if(null != adv){
                otcCoin = otcCoinService.getById(adv.getCoinId());
                if(null == otcCoin){
                    continue;
                }
                finalPrice = coins.get(otcCoin.getUnit(),currency).doubleValue();
                if(null != adv.getPremiseRate()){
                    adv.setPrice(BigDecimalUtils.round(((adv.getPremiseRate().doubleValue() + 100) / 100) * finalPrice,2));
                }
                adv.setUnit(otcCoin.getUnit());
                adv.setCoinName(otcCoin.getName());
                adv.setCoinNameCn(otcCoin.getNameCn());
            }
        }
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(list);
        return messageResult;
    }
}
