package com.wikex.wikex.p2p.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.exchange.entity.OrderDetailAggregation;
import com.wikex.wikex.exchange.feign.OrderDetailAggregationFeign;
import com.wikex.wikex.p2p.config.CoinExchangeFactory;
import com.wikex.wikex.p2p.entity.*;
import com.wikex.wikex.p2p.entity.chat.ChatMessageRecord;
import com.wikex.wikex.p2p.event.OrderEvent;
import com.wikex.wikex.p2p.service.AdvertiseService;
import com.wikex.wikex.p2p.service.AppealService;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.p2p.service.OtcOrderService;
import com.wikex.wikex.p2p.vo.AppealApply;
import com.wikex.wikex.p2p.vo.OrderDetail;
import com.wikex.wikex.p2p.vo.PayInfo;
import com.wikex.wikex.p2p.vo.ScanOrder;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.feign.*;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static com.wikex.wikex.constant.BooleanEnum.IS_FALSE;
import static com.wikex.wikex.constant.BooleanEnum.IS_TRUE;
import static com.wikex.wikex.util.BigDecimalUtils.*;

@Api(tags = "OTC orders")
@RestController
@RequestMapping(value = "/order", method = RequestMethod.POST)
@Slf4j
public class OrderController extends BaseController {

    @Autowired
    private OtcOrderService orderService;
    @Autowired
    private PaymentFeign paymentFeign;
    @Autowired
    private OtcCoinService otcCoinService;

    @Autowired
    private AdvertiseService advertiseService;

    @Autowired
    private MemberFeign memberService;

    @Autowired
    private MemberWalletFeign memberWalletService;

    @Autowired
    private CoinExchangeFactory coins;

    @Autowired
    private OrderEvent orderEvent;

    @Autowired
    private AppealService appealService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private OrderDetailAggregationFeign orderDetailAggregationService;

    @Autowired
    private MemberTransactionFeign memberTransactionService;
    @Autowired
    private CountryFeign countryService;

    @Value("${spark.system.order.sms:1}")
    private int notice;

    @Autowired
    private SMSProvider smsProvider;
    @Autowired
    private CountryFeign countryFeign;
    @Autowired
    private MongoTemplate mongoTemplate;

    private String[] colors = { "#f0a70a", "#e5dc2a", "#4fbe51", "#d07e3b", "#0a4bf0", "#810af0", "#2b9f76" };

    @ApiOperation(value = "Buy/Sell order details")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id")
    })
    @RequestMapping(value = "pre", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public MessageResult preOrderInfo(long id) {
        Advertise advertise = advertiseService.getById(id);
        notNull(advertise, msService.getMessage("PARAMETER_ERROR"));
        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES.getCode()),
                msService.getMessage("PARAMETER_ERROR"));
        Member member = memberService.findMemberById(advertise.getMemberId());
        OtcCoin otcCoin = otcCoinService.getById(advertise.getCoinId());
        Country country1 = countryFeign.findByZhName(advertise.getCountry());
        PreOrderInfo preOrderInfo = PreOrderInfo.builder()
                .advertiseType(advertise.getAdvertiseType())
                .country(advertise.getCountry())
                .currency(country1 != null ? country1.getLocalCurrency() : "")
                .emailVerified(member.getEmail() == null ? IS_FALSE.getCode() : IS_TRUE.getCode())
                .idCardVerified(member.getIdNumber() == null ? IS_FALSE.getCode() : IS_TRUE.getCode())
                .maxLimit(advertise.getMaxLimit())
                .minLimit(advertise.getMinLimit())
                .number(advertise.getRemainAmount())
                .otcCoinId(otcCoin.getId())
                .payMode(advertise.getPayMode())
                .phoneVerified(member.getMobilePhone() == null ? IS_FALSE.getCode() : IS_TRUE.getCode())
                .timeLimit(advertise.getTimeLimit() == null ? 0 : advertise.getTimeLimit())
                .transactions(member.getTransactions())
                .unit(otcCoin.getUnit())
                .username(member.getUsername())
                .remark(advertise.getRemark())
                .build();

        if (advertise.getAdvertiseType().equals(AdvertiseType.SELL.getCode())) {
            BigDecimal maxTransactions = divDown(advertise.getRemainAmount(),
                    add(BigDecimal.ONE, getRate(otcCoin.getJyRate())));
            preOrderInfo.setMaxTradableAmount(maxTransactions);
        } else {
            preOrderInfo.setMaxTradableAmount(advertise.getRemainAmount());
        }
        if (advertise.getPriceType().equals(PriceType.REGULAR.getCode())) {
            preOrderInfo.setPrice(advertise.getPrice());
        } else {
            Country country = countryFeign.findByZhName(advertise.getCountry());
            BigDecimal marketPrice = coins.get(otcCoin.getUnit(), country.getLocalCurrency());
            preOrderInfo.setPrice(mulRound(marketPrice, rate(advertise.getPremiseRate()), 2));
        }
        MessageResult result = MessageResult.success();
        result.setData(preOrderInfo);
        return result;
    }

    @ApiOperation(value = "Buy coin")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
            @ApiImplicitParam(name = "coinId", value = "Coin type"),
            @ApiImplicitParam(name = "price", value = "Price"),
            @ApiImplicitParam(name = "money", value = "Trade amount"),
            @ApiImplicitParam(name = "amount", value = "Closing amount"),
            @ApiImplicitParam(name = "remark", value = "Customer request"),
    })
    @RequestMapping(value = "buy", method = RequestMethod.POST)
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult buy(long id, long coinId, BigDecimal price, BigDecimal money,
            BigDecimal amount, String remark,
            @RequestParam(value = "mode", defaultValue = "0") Integer mode,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws InformationExpiredException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Advertise advertise = advertiseService.getById(id);
        if (advertise == null || !advertise.getAdvertiseType().equals(AdvertiseType.SELL.getCode())) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }

        Member owner = memberService.findMemberById(advertise.getMemberId());

        isTrue(!user.getName().equals(owner.getUsername()), msService.getMessage("NOT_ALLOW_BUY_BY_SELF"));
        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES.getCode()),
                msService.getMessage("ALREADY_PUT_OFF"));
        OtcCoin otcCoin = otcCoinService.getById(advertise.getCoinId());
        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        if (advertise.getPriceType().equals(PriceType.REGULAR.getCode())) {
            isTrue(isEqual(price, advertise.getPrice()), msService.getMessage("PRICE_EXPIRED"));
        } else {
            Country country = countryFeign.findByZhName(advertise.getCountry());
            BigDecimal marketPrice = coins.get(otcCoin.getUnit(), country.getLocalCurrency());
            isTrue(isEqual(price, mulRound(rate(advertise.getPremiseRate()), marketPrice, 2)),
                    msService.getMessage("PRICE_EXPIRED"));
        }
        if (mode == 0) {
            isTrue(isEqual(div(money, price), amount), msService.getMessage("NUMBER_ERROR"));
        } else {
            isTrue(isEqual(mulRound(amount, price, 2), money), msService.getMessage("NUMBER_ERROR"));
        }
        isTrue(compare(money, advertise.getMinLimit()),
                msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString() + " CNY");
        isTrue(compare(advertise.getMaxLimit(), money),
                msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString() + " CNY");
        String[] pay = advertise.getPayMode().split(",");

        BigDecimal commission = mulRound(amount, getRate(otcCoin.getJyRate()));

        Member member = memberService.findMemberById(user.getId());

        if (member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED)
                && member.getMemberLevel().equals(MemberLevelEnum.IDENTIFICATION.getCode())) {
            commission = BigDecimal.ZERO;
        }

        isTrue(compare(advertise.getRemainAmount(), amount), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        OtcOrder order = new OtcOrder();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(AdvertiseType.creator(advertise.getAdvertiseType()));
        order.setCoinId(otcCoin.getId());
        order.setCommission(commission);
        order.setCountry(advertise.getCountry());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(member.getRealName());
        order.setMemberId(owner.getId());
        order.setMemberName(owner.getUsername());
        order.setMemberRealName(owner.getRealName());
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(sub(amount, commission));
        order.setPayMode(advertise.getPayMode());
        order.setPrice(price);
        order.setRemark(remark);
        order.setTimeLimit(advertise.getTimeLimit());
        order.setCreateTime(new Date());
        Member ader = memberService.findMemberById(advertise.getMemberId());
        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), amount)) {
            throw new InformationExpiredException("Information Expired");
        }
        OtcOrder order1 = orderService.saveOrder(order);
        if (order1 != null) {
            if (notice == 1) {
                try {
                    smsProvider.sendMessageByTempId(ader.getMobilePhone(), otcCoin.getUnit() + "##" + user.getName(),
                            "9499");
                } catch (Exception e) {
                    log.error("sms send failed");
                    e.printStackTrace();
                }
            }

            if (advertise.getAuto() == BooleanEnum.IS_TRUE.getCode()) {
                ChatMessageRecord chatMessageRecord = new ChatMessageRecord();
                chatMessageRecord.setOrderId(order1.getOrderSn());
                chatMessageRecord.setUidFrom(order1.getMemberId().toString());
                chatMessageRecord.setUidTo(order1.getCustomerId().toString());
                chatMessageRecord.setNameFrom(order1.getMemberName());
                chatMessageRecord.setNameTo(order1.getCustomerName());
                chatMessageRecord.setContent(advertise.getAutoword());
                chatMessageRecord.setSendTime(Calendar.getInstance().getTimeInMillis());
                chatMessageRecord.setSendTimeStr(DateUtil.getDateTime());

                mongoTemplate.insert(chatMessageRecord, "chat_message");
            }
            MessageResult result = MessageResult.success(msService.getMessage("CREATE_ORDER_SUCCESS"));
            result.setData(order1.getOrderSn().toString());
            return result;
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

    @ApiOperation(value = "Sell coin")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
            @ApiImplicitParam(name = "coinId", value = "Coin type"),
            @ApiImplicitParam(name = "price", value = "Price"),
            @ApiImplicitParam(name = "money", value = "Trade amount"),
            @ApiImplicitParam(name = "amount", value = "Closing amount"),
            @ApiImplicitParam(name = "remark", value = "Customer request"),
    })
    @PermissionOperation
    @RequestMapping(value = "sell")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sell(long id, long coinId, BigDecimal price, BigDecimal money,
            BigDecimal amount, String remark,
            @RequestParam(value = "mode", defaultValue = "0") Integer mode,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws InformationExpiredException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Advertise advertise = advertiseService.getById(id);
        if (advertise == null || !advertise.getAdvertiseType().equals(AdvertiseType.BUY.getCode())) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        Member adver = memberService.findMemberById(advertise.getMemberId());
        isTrue(!user.getName().equals(adver.getUsername()), msService.getMessage("NOT_ALLOW_SELL_BY_SELF"));
        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES.getCode()),
                msService.getMessage("ALREADY_PUT_OFF"));
        OtcCoin otcCoin = otcCoinService.getById(advertise.getCoinId());
        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        if (advertise.getPriceType().equals(PriceType.REGULAR.getCode())) {
            isTrue(isEqual(price, advertise.getPrice()), msService.getMessage("PRICE_EXPIRED"));
        } else {
            Country country = countryFeign.findByZhName(advertise.getCountry());
            BigDecimal marketPrice = coins.get(otcCoin.getUnit(), country.getLocalCurrency());
            isTrue(isEqual(price, mulRound(rate(advertise.getPremiseRate()), marketPrice, 2)),
                    msService.getMessage("PRICE_EXPIRED"));
        }
        if (mode == 0) {
            isTrue(isEqual(div(money, price), amount), msService.getMessage("NUMBER_ERROR"));
        } else {
            isTrue(isEqual(mulRound(amount, price, 2), money), msService.getMessage("NUMBER_ERROR"));
        }
        isTrue(compare(money, advertise.getMinLimit()),
                msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString() + " CNY");
        isTrue(compare(advertise.getMaxLimit(), money),
                msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString() + " CNY");

        BigDecimal commission = mulRound(amount, getRate(otcCoin.getJyRate()));

        if (adver.getCertifiedBusinessStatus() == CertifiedBusinessStatus.VERIFIED
                && adver.getMemberLevel() == MemberLevelEnum.IDENTIFICATION.getCode()) {
            commission = BigDecimal.ZERO;
        }

        isTrue(compare(advertise.getRemainAmount(), amount), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), user.getId());
        isTrue(compare(wallet.getBalance(), amount), msService.getMessage("INSUFFICIENT_BALANCE"));
        Member member = memberService.findMemberById(user.getId());
        OtcOrder order = new OtcOrder();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(AdvertiseType.creator(advertise.getAdvertiseType()));
        order.setCoinId(otcCoin.getId());
        order.setCommission(commission);
        order.setCountry(advertise.getCountry());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(member.getRealName());
        order.setMemberId(adver.getId());
        order.setMemberName(adver.getUsername());
        order.setMemberRealName(adver.getRealName());
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setPrice(price);
        order.setRemark(remark);
        order.setTimeLimit(advertise.getTimeLimit());
        order.setCreateTime(new Date());
        String[] pay = advertise.getPayMode().split(",");
        MessageResult result = MessageResult.error(msService.getMessage("CREATE_ORDER_SUCCESS"));
        isTrue(result.getCode() == 0, msService.getMessage("AT_LEAST_SUPPORT_PAY"));
        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), amount)) {
            throw new InformationExpiredException("Information Expired");
        }
        if (!(memberWalletService.freezeBalance(wallet.getId(), amount).getCode() == 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        OtcOrder order1 = orderService.saveOrder(order);
        if (order1 != null) {
            if (notice == 1) {
                try {
                    smsProvider.sendMessageByTempId(adver.getMobilePhone(), otcCoin.getUnit() + "##" + user.getName(),
                            "9499");
                } catch (Exception e) {
                    log.error("sms send failed");
                    e.printStackTrace();
                }
            }
            result.setData(order1.getOrderSn().toString());
            return result;
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

    @ApiOperation(value = "My orders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "Status"),
            @ApiImplicitParam(name = "orderSn", value = "Order number"),
    })
    @PermissionOperation
    @RequestMapping(value = "self")
    public MessageResult myOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, OrderStatus status,
            int pageNo, int pageSize, String orderSn) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Page<OtcOrder> page = orderService.pageQuery(pageNo, pageSize, status, user.getId(), orderSn);
        List<Long> memberIdList = new ArrayList<>();
        page.getRecords().forEach(order -> {
            if (!memberIdList.contains(order.getMemberId())) {
                memberIdList.add(order.getMemberId());
            }
            if (!memberIdList.contains(order.getCustomerId())) {
                memberIdList.add(order.getCustomerId());
            }
        });
        org.springframework.data.domain.Page<ScanOrder> scanOrders = IPage2Page(page).map(x -> {
            OtcCoin otcCoin = otcCoinService.getById(x.getCoinId());
            Country country = countryFeign.findByZhName(x.getCountry());
            String currency = country == null ? "" : country.getLocalCurrency();
            return ScanOrder.toScanOrder(x, user.getId(), otcCoin.getUnit(), currency);
        });
        if (scanOrders != null) {
            for (ScanOrder scanOrder : scanOrders) {
                Member member = memberService.findMemberById(scanOrder.getMemberId());
                scanOrder.setAvatar(member.getAvatar());
            }
        } else {
            scanOrders = org.springframework.data.domain.Page.empty();
        }
        MessageResult result = MessageResult.success();
        result.setData(scanOrders);
        return result;
    }

    @ApiOperation(value = "Order Detail")
    @PermissionOperation
    @RequestMapping(value = "detail")
    public MessageResult queryOrder(String orderSn, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        OtcOrder order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        MessageResult result = MessageResult.success();
        Member member = memberService.findMemberById(order.getMemberId());
        OtcCoin otcCoin = otcCoinService.getById(order.getCoinId());

        Advertise advertise = advertiseService.getById(order.getAdvertiseId());
        Long seller = advertise.getMemberId();
        if (advertise.getAdvertiseType().equals(AdvertiseType.BUY.getCode())) {
            seller = order.getCustomerId();
        }
        List<PaymentTypeRecord> records = paymentFeign.getRecordsByUserId(seller);

        String payMode = advertise.getPayMode();
        List<PaymentTypeRecord> payInfos = new ArrayList<>();
        int index = 0;
        if (!StringUtils.isEmpty(payMode)) {
            String[] type = payMode.split(",");
            List<String> types = Arrays.asList(type);
            for (PaymentTypeRecord record : records) {
                if (types.contains(record.getTypeName())) {
                    record.setColor(colors[index % 6]);
                    index++;
                    payInfos.add(record);
                }
            }
        }

        OrderDetail info = OrderDetail.builder().orderSn(orderSn)
                .unit(otcCoin.getUnit())
                .status(order.getStatus().getCode())
                .amount(order.getNumber())
                .price(order.getPrice())
                .payInfos(payInfos)
                .money(order.getMoney())
                .payTime(order.getPayTime())
                .createTime(order.getCreateTime())
                .timeLimit(order.getTimeLimit())
                .myId(user.getId()).memberMobile(member.getMobilePhone())
                .build();
        /* if (!order.getStatus().equals(OrderStatus.CANCELLED)) { */
        PayInfo payInfo = PayInfo.builder()
                .bankInfo(order.getBankInfo())
                .alipay(order.getAlipay())
                .wechatPay(order.getWechatPay())
                .build();
        info.setPayInfo(payInfo);
        if (StringUtils.isNotEmpty(order.getCountry())) {
            Country country = countryFeign.findByZhName(order.getCountry());
            if (country != null) {
                info.setCurrency(country.getLocalCurrency());
            }
        }
        /* } */
        if (order.getMemberId().equals(user.getId())) {
            info.setHisId(order.getCustomerId());
            info.setOtherSide(order.getCustomerName());
            info.setCommission(order.getCommission());
            Member memberCustomer = memberService.findMemberById(order.getCustomerId());
            info.setMemberMobile(memberCustomer.getMobilePhone());
            if (order.getAdvertiseType().equals(AdvertiseType.BUY)) {
                info.setType(AdvertiseType.BUY.getCode());
                if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getCustomerRealName());
                }
            } else {
                info.setType(AdvertiseType.SELL.getCode());
                if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getMemberRealName());
                }
            }
        } else if (order.getCustomerId().equals(user.getId())) {
            info.setHisId(order.getMemberId());
            info.setOtherSide(order.getMemberName());
            info.setCommission(BigDecimal.ZERO);
            Member memberOrder = memberService.findMemberById(order.getMemberId());
            info.setMemberMobile(memberOrder.getMobilePhone());
            if (order.getAdvertiseType().equals(AdvertiseType.BUY)) {
                if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getCustomerRealName());
                }
                info.setType(AdvertiseType.SELL.getCode());
            } else {
                if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getMemberRealName());
                }
                info.setType(AdvertiseType.BUY.getCode());
            }
        } else {
            return MessageResult.error(msService.getMessage("ORDER_NOT_EXISTS"));
        }
        result.setData(info);
        return result;
    }

    @ApiOperation(value = "Cancel Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "Order Number")
    })
    @PermissionOperation
    @RequestMapping(value = "cancel")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrder(String orderSn, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember)
            throws InformationExpiredException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        OtcOrder order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(user.getId())) {
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID),
                msService.getMessage("ORDER_NOT_ALLOW_CANCEL"));
        MemberWallet memberWallet;

        if (!(orderService.cancelOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        OtcCoin otcCoin = otcCoinService.getById(order.getCoinId());
        if (ret == 1) {
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException("Information Expired");
            }
            memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), order.getCustomerId());
            MessageResult result = memberWalletService.thawBalance(memberWallet.getCoinId(), memberWallet.getMemberId(),
                    order.getNumber());
            if (result.getCode() == 0) {
                return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"));
            } else {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(),
                    add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException("Information Expired");
            }
            memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), order.getMemberId());
            MessageResult result = memberWalletService.thawBalance(memberWallet.getCoinId(), memberWallet.getMemberId(),
                    add(order.getNumber(), order.getCommission()));
            if (result.getCode() == 0) {
                return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"));
            } else {
                throw new InformationExpiredException("Information Expired");
            }
        }
    }

    @ApiOperation(value = "Confirm Payment")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "Order Number")
    })
    @PermissionOperation
    @RequestMapping(value = "pay")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult payOrder(String orderSn, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember)
            throws InformationExpiredException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        OtcOrder order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(user.getId())) {
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT), msService.getMessage("ORDER_STATUS_EXPIRED"));
        if (order.getTimeLimit() != null) {
            isTrue(compare(new BigDecimal(order.getTimeLimit()), DateUtil.diffMinute(order.getCreateTime())),
                    msService.getMessage("ORDER_ALREADY_AUTO_CANCEL"));
        }

        int is = orderService.payForOrder(orderSn);
        if (is > 0) {
            OtcCoin otcCoin = otcCoinService.getById(order.getCoinId());
            OrderDetailAggregation aggregation = new OrderDetailAggregation();
            BeanUtils.copyProperties(order, aggregation);
            aggregation.setUnit(otcCoin.getUnit());
            aggregation.setOrderId(order.getOrderSn());
            aggregation.setFee(order.getCommission().doubleValue());
            aggregation.setAmount(order.getNumber().doubleValue());
            aggregation.setType(OrderTypeEnum.OTC);
            aggregation.setTime(Calendar.getInstance().getTimeInMillis());
            orderDetailAggregationService.save(aggregation);

            MessageResult result = MessageResult.success(msService.getMessage("PAY_SUCCESS"));
            result.setData(order);
            return result;
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

    @ApiOperation(value = "Release Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "Order Number"),
            @ApiImplicitParam(name = "jyPassword", value = "Transaction Password")
    })
    @PermissionOperation
    @RequestMapping(value = "release")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult confirmRelease(String orderSn, String jyPassword,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findMemberById(user.getId());
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(MD5.md5(jyPassword + member.getSalt()).toLowerCase().equals(mbPassword),
                msService.getMessage("ERROR_JYPASSWORD"));
        OtcOrder order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getCustomerId().equals(user.getId())) {
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getMemberId().equals(user.getId())) {
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.PAID), msService.getMessage("ORDER_STATUS_EXPIRED"));
        if (ret == 1) {
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(),
                    add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException("Information Expired");
            }
        }

        if (!(orderService.releaseOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        OtcCoin otcCoin = otcCoinService.getById(order.getCoinId());

        this.transfer(otcCoin, order, ret);
        MemberTransaction memberTransaction = new MemberTransaction();
        MemberTransaction memberTransaction1 = new MemberTransaction();
        if (ret == 1) {
            memberTransaction.setSymbol(otcCoin.getUnit());
            memberTransaction.setType(TransactionType.OTC_SELL.getCode());
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setMemberId(user.getId());
            memberTransaction.setAmount(order.getNumber());
            memberTransaction.setDiscountFee("0");
            memberTransaction.setRealFee("0");
            memberTransaction.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction);

            memberTransaction1.setAmount(order.getNumber());
            memberTransaction1.setType(TransactionType.OTC_BUY.getCode());
            memberTransaction1.setMemberId(order.getMemberId());
            memberTransaction1.setSymbol(otcCoin.getUnit());
            memberTransaction1.setFee(order.getCommission());
            memberTransaction1.setDiscountFee("0");
            memberTransaction1.setRealFee(order.getCommission() + "");
            memberTransaction1.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction1);
        } else {
            memberTransaction.setSymbol(otcCoin.getUnit());
            memberTransaction.setType(TransactionType.OTC_SELL.getCode());
            memberTransaction.setFee(order.getCommission());
            memberTransaction.setMemberId(user.getId());
            memberTransaction.setAmount(order.getNumber());
            memberTransaction.setDiscountFee("0");
            memberTransaction.setRealFee(order.getCommission() + "");
            memberTransaction.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction);

            memberTransaction1.setAmount(order.getNumber());
            memberTransaction1.setType(TransactionType.OTC_BUY.getCode());
            memberTransaction1.setMemberId(order.getCustomerId());
            memberTransaction1.setSymbol(otcCoin.getUnit());
            memberTransaction1.setFee(BigDecimal.ZERO);
            memberTransaction1.setDiscountFee("0");
            memberTransaction1.setRealFee(order.getCommission() + "");
            memberTransaction1.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction1);
        }
        orderEvent.onOrderCompleted(order);
        return MessageResult.success(msService.getMessage("RELEASE_SUCCESS"));
    }

    public void transfer(OtcCoin otcCoin, OtcOrder order, int ret) throws InformationExpiredException {
        if (ret == 1) {
            MemberWallet customerWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(),
                    order.getCustomerId());
            MessageResult is = memberWalletService.decreaseFrozen(customerWallet.getId(), order.getNumber());
            if (is.getCode() == 0) {
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(),
                        order.getMemberId());
                MessageResult a = memberWalletService.increaseBalance(memberWallet.getId(),
                        BigDecimalUtils.sub(order.getNumber(), order.getCommission()));
                if (a.getCode() != 0) {
                    throw new InformationExpiredException("Information Expired");
                }
            } else {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            MemberWallet customerWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(),
                    order.getMemberId());
            MessageResult is = memberWalletService.decreaseFrozen(customerWallet.getId(),
                    BigDecimalUtils.add(order.getNumber(), order.getCommission()));
            if (is.getCode() == 0) {
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(),
                        order.getCustomerId());
                MessageResult a = memberWalletService.increaseBalance(memberWallet.getId(), order.getNumber());
                if (a.getCode() != 0) {
                    throw new InformationExpiredException("Information Expired");
                }
            } else {
                throw new InformationExpiredException("Information Expired");
            }
        }
    }

    @ApiOperation(value = "Appeal")
    @PermissionOperation
    @RequestMapping(value = "appeal")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult appeal(@Valid AppealApply appealApply, BindingResult bindingResult,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws InformationExpiredException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        OtcOrder order = orderService.findOneByOrderSn(appealApply.getOrderSn());
        int ret = 0;
        if (order.getMemberId().equals(user.getId())) {
            ret = 1;
        } else if (order.getCustomerId().equals(user.getId())) {
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.PAID), msService.getMessage("NO_APPEAL"));
        if (!(orderService.updateOrderAppeal(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        Appeal appeal = new Appeal();
        appeal.setInitiatorId(user.getId());
        if (ret == 1) {
            appeal.setAssociateId(order.getCustomerId());
        } else {
            appeal.setAssociateId(order.getMemberId());
        }
        appeal.setOrderId(order.getId());
        appeal.setRemark(appealApply.getRemark());
        appeal.setStatus(AppealStatus.NOT_PROCESSED);
        appeal.setCreateTime(new Date());
        boolean isOk = appealService.save(appeal);
        if (isOk) {
            return MessageResult.success(msService.getMessage("APPEAL_SUCCESS"));
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

}
