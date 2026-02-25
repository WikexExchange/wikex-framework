package com.wikex.wikex.p2p.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.AdvertiseControlStatus;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.PriceType;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.p2p.entity.Advertise;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.mapper.AdvertiseMapper;
import com.wikex.wikex.p2p.service.AdvertiseService;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.p2p.vo.AdvertiseVo;
import com.wikex.wikex.p2p.vo.MemberAdvertiseDetail;
import com.wikex.wikex.p2p.vo.MemberAdvertiseInfo;
import com.wikex.wikex.p2p.vo.ScanAdvertise;
import com.wikex.wikex.screen.AdvertiseScreen;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CountryFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.BigDecimalUtils;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.wikex.wikex.constant.BooleanEnum.IS_FALSE;
import static com.wikex.wikex.constant.BooleanEnum.IS_TRUE;
import static com.wikex.wikex.util.BigDecimalUtils.mulRound;
import static com.wikex.wikex.util.BigDecimalUtils.rate;


@Service
public class AdvertiseServiceImpl extends ServiceImpl<AdvertiseMapper, Advertise> implements AdvertiseService {

    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private OtcCoinService coinService;
    @Autowired
    private CountryFeign countryFeign;
    @Autowired
    private MemberFeign memberFeign;

    @Override
    public int turnOffBatch(AdvertiseControlStatus status, Long[] ids) {
        return this.baseMapper.alterStatusBatch(status.getCode(), new Date(), ids);
    }

    @Override
    public Page<Advertise> findAll(AdvertiseScreen screen) {
        Page<Advertise> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        LambdaQueryWrapper<Advertise> query = new LambdaQueryWrapper<>();
        if(screen.getStatus()!=AdvertiseControlStatus.TURNOFF && screen.getStatus()!=null) {
            query.eq(Advertise::getStatus,screen.getStatus().getCode());
        }
        if (screen.getMemberId()!=null){
            query.eq(Advertise::getMemberId,screen.getMemberId());
        }
        if (screen.getStatus() == null){
            query.and(wrapper->wrapper.eq(Advertise::getStatus,AdvertiseControlStatus.PUT_ON_SHELVES.getCode()).or().eq(Advertise::getStatus,AdvertiseControlStatus.PUT_OFF_SHELVES.getCode()));
        }
        if (screen.getAdvertiseType() != null) {
            query.eq(Advertise::getAdvertiseType,screen.getAdvertiseType().getCode());
        }

        if(screen.getPayModel()!=null) {
            query.like(Advertise::getPayMode,screen.getPayModel());
        }
        return this.page(page,query);
    }

    @Override
    public List<Advertise> queryAdvertise(Date startTime, Date endTime, AdvertiseType advertiseType, String realName) {

        LambdaQueryWrapper<Advertise> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Advertise::getStatus,AdvertiseControlStatus.PUT_ON_SHELVES.getCode(), AdvertiseControlStatus.PUT_OFF_SHELVES.getCode());
        if (startTime != null) {
            queryWrapper.gt(Advertise::getCreateTime,startTime);
        }
        if (endTime != null) {
            queryWrapper.lt(Advertise::getCreateTime,endTime);
        }
        if (advertiseType != null) {
            queryWrapper.eq(Advertise::getAdvertiseType,advertiseType.getCode());
        }





        return this.list(queryWrapper);
    }

    @Override
    public List<AdvertiseVo> selectSellAutoOffShelves(Long coinId, BigDecimal marketPrice, BigDecimal jyRate) {
        List<AdvertiseVo> list = this.baseMapper.selectSellAutoOffShelves(marketPrice, coinId, jyRate);
        return list;
    }

    @Override
    public List<AdvertiseVo> selectBuyAutoOffShelves(Long coinId, BigDecimal marketPrice) {
        List<AdvertiseVo> list = this.baseMapper.selectBuyAutoOffShelves(marketPrice, coinId);
        return list;
    }

    @Override
    @Transactional
    public void autoPutOffShelves(AdvertiseVo vo, OtcCoin otcCoin) throws InformationExpiredException {
        if (vo.getAdvertiseType().equals(AdvertiseType.SELL)) {
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), vo.getMemberId());
            MessageResult result = memberWalletService.thawBalance(memberWallet.getCoinId(),memberWallet.getMemberId(),vo.getRemainAmount());
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        }
        int is = putOffShelves(vo.getId(), vo.getRemainAmount());
        if (!(is > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
    }

    @Override
    public boolean updateAdvertiseAmountForCancel(Long advertiseId, BigDecimal amount) {
        int ret = this.baseMapper.updateAdvertiseDealAmount(advertiseId, amount);
        return ret > 0 ? true : false;
    }

    @Override
    public boolean updateAdvertiseAmountForBuy(Long advertiseId, BigDecimal amount) {
        int ret = this.baseMapper.updateAdvertiseAmount(AdvertiseControlStatus.PUT_ON_SHELVES.getCode(), advertiseId, amount);
        return ret > 0 ? true : false;
    }

    @Override
    public boolean updateAdvertiseAmountForRelease(Long advertiseId, BigDecimal amount) {
        int ret = this.baseMapper.updateAdvertiseDealAmount(advertiseId, amount);
        return ret > 0 ? true : false;
    }

    @Override
    public Long getAdvertiserNum(Long memberId) {
        return this.baseMapper.getAdvertiserNum(memberId);
    }

    @Override
    public MemberAdvertiseDetail findOne(Long id, Long memberId) {
        LambdaQueryWrapper<Advertise> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Advertise::getId,id);
        queryWrapper.eq(Advertise::getMemberId,memberId);
        queryWrapper.ne(Advertise::getStatus,AdvertiseControlStatus.TURNOFF.getCode());
        Advertise advertise = this.getOne(queryWrapper);
        if (advertise != null) {
            OtcCoin coin = coinService.getById(advertise.getCoinId());
            Country country = countryFeign.findByZhName(advertise.getCountry());
            return MemberAdvertiseDetail.toMemberAdvertiseDetail(advertise,coin,country);
        } else {
            return null;
        }
    }

    @Override
    public Advertise modifyAdvertise(Advertise advertise, Advertise old) {
        if (advertise.getPriceType() == PriceType.MUTATIVE.getCode()) {
            
            old.setPriceType(PriceType.MUTATIVE.getCode());
            old.setPremiseRate(advertise.getPremiseRate());
        } else {
            
            old.setPriceType(PriceType.REGULAR.getCode());
            old.setPrice(advertise.getPrice());
        }
        if (advertise.getAuto()==1) {
            old.setAuto(IS_TRUE.getCode());
            old.setAutoword(advertise.getAutoword());
        } else {
            old.setAuto(IS_FALSE.getCode());
        }
        old.setMinLimit(advertise.getMinLimit());
        old.setMaxLimit(advertise.getMaxLimit());
        old.setTimeLimit(advertise.getTimeLimit());
        old.setRemark(advertise.getRemark());
        old.setPayMode(advertise.getPayMode());
        old.setNumber(advertise.getNumber());
        old.setRemainAmount(advertise.getNumber());
        
        old.setStatus(AdvertiseControlStatus.PUT_OFF_SHELVES.getCode());
        this.updateById(old);
        return old;
    }

    @Override
    public Advertise find(Long id, Long memberId) {
        LambdaQueryWrapper<Advertise> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Advertise::getId,id);
        queryWrapper.eq(Advertise::getMemberId,memberId);
        return this.getOne(queryWrapper);
    }

    @Override
    public int putOffShelves(Long id, BigDecimal amount) {
        return  this.baseMapper.putOffAdvertise(id,amount);
    }

    @Override
    public MemberAdvertiseInfo getMemberAdvertise(Member member, HashMap<String,HashMap<String,BigDecimal>> coins) {
        LambdaQueryWrapper<Advertise> query = new LambdaQueryWrapper<>();
        query.eq(Advertise::getMemberId,member.getId());
        query.eq(Advertise::getStatus,AdvertiseControlStatus.PUT_ON_SHELVES.getCode());
        query.eq(Advertise::getAdvertiseType,AdvertiseType.BUY.getCode());
        List<Advertise> buy = this.list(query);
        query.eq(Advertise::getAdvertiseType,AdvertiseType.SELL.getCode());
        List<Advertise> sell = this.list(query);
        return MemberAdvertiseInfo.builder()
                .createTime(member.getRegistrationTime())
                .emailVerified(StringUtils.isEmpty(member.getEmail()) ? IS_FALSE : IS_TRUE)
                .phoneVerified(StringUtils.isEmpty(member.getMobilePhone()) ? IS_FALSE : IS_TRUE)
                .realVerified(StringUtils.isEmpty(member.getRealName()) ? IS_FALSE : IS_TRUE)
                .transactions(member.getTransactions())
                .username(member.getUsername())
                .avatar(member.getAvatar())
                .buy(buy.stream().map(advertise -> {
                    OtcCoin coin = coinService.getById(advertise.getCoinId());
                    Country country = countryFeign.findByZhName(advertise.getCountry());
                    BigDecimal markerPrice = coins.get(coin.getUnit()).get(country.getLocalCurrency());
                    Member member1 = memberFeign.findMemberById(advertise.getMemberId());
                    return ScanAdvertise.builder()
                            .advertiseId(advertise.getId())
                            .coinId(advertise.getCoinId())
                            .coinName(coin.getName())
                            .coinNameCn(coin.getNameCn())
                            .createTime(advertise.getCreateTime())
                            .maxLimit(advertise.getMaxLimit())
                            .minLimit(advertise.getMinLimit())
                            .memberName(member1.getUsername())
                            .payMode(advertise.getPayMode())
                            .unit(coin.getUnit())
                            .remainAmount(advertise.getRemainAmount())
                            .transactions(member1.getTransactions())
                            .localCurrency(country.getLocalCurrency())
                            .price(advertise.getPriceType().equals(PriceType.REGULAR.getCode()) ?
                                    advertise.getPrice() :
                                    mulRound(markerPrice, rate(advertise.getPremiseRate()), 2))
                            .build();
                }).collect(Collectors.toList()))
                .sell(sell.stream().map(advertise -> {
                    OtcCoin coin = coinService.getById(advertise.getCoinId());
                    Country country = countryFeign.findByZhName(advertise.getCountry());
                    BigDecimal markerPrice = coins.get(coin.getUnit()).get(country.getLocalCurrency());
                    Member member1 = memberFeign.findMemberById(advertise.getMemberId());
                    return ScanAdvertise.builder()
                            .advertiseId(advertise.getId())
                            .coinId(advertise.getCoinId())
                            .coinName(coin.getName())
                            .coinNameCn(coin.getNameCn())
                            .createTime(advertise.getCreateTime())
                            .maxLimit(advertise.getMaxLimit())
                            .minLimit(advertise.getMinLimit())
                            .memberName(member1.getUsername())
                            .payMode(advertise.getPayMode())
                            .unit(coin.getUnit())
                            .remainAmount(advertise.getRemainAmount())
                            .transactions(member1.getTransactions())
                            .localCurrency(country.getLocalCurrency())
                            .price(advertise.getPriceType().equals(PriceType.REGULAR.getCode()) ?
                                    advertise.getPrice() : mulRound(markerPrice, rate(advertise.getPremiseRate()), 2)
                            )
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }

    @Override
    public Page<ScanAdvertise> paginationAdvertise(Integer pageNo, Integer pageSize,String country, OtcCoin otcCoin, AdvertiseType advertiseType, double marketPrice, Integer isCertified) {
        Page<Advertise> page = new Page<>(pageNo,pageSize);
        Page<Advertise> list = this.baseMapper.paginationAdvertise(page,marketPrice,country,otcCoin.getId(),advertiseType.getCode());
        for (Advertise record : list.getRecords()) {
            Member member = memberFeign.findMemberById(record.getMemberId());
            record.setAvatar(member.getAvatar());
            record.setUsername(member.getUsername());
            record.setMemberLevel(member.getMemberLevel());
            record.setTransactions(member.getTransactions());
        }







































        Page<ScanAdvertise> specialPage = new Page<>(pageNo,pageSize);
        specialPage.setTotal(list.getTotal());
        specialPage.setSize(list.getSize());
        specialPage.setOrders(list.getOrders());
        specialPage.setCurrent(list.getCurrent());
        specialPage.setHitCount(list.isHitCount());
        specialPage.setPages(list.getPages());
        specialPage.setRecords(
                list.getRecords().stream().map((x) ->
                        ScanAdvertise.builder()
                                .price(BigDecimalUtils.round(x.getFinalPrice(), 2))
                                .transactions(x.getTransactions())
                                .remainAmount(x.getRemainAmount())
                                .unit(otcCoin.getUnit())
                                .payMode(x.getPayMode())
                                .memberName(x.getUsername())
                                .avatar(x.getAvatar())
                                .minLimit(x.getMinLimit())
                                .maxLimit(x.getMaxLimit())
                                .coinNameCn(otcCoin.getNameCn())
                                .level(x.getMemberLevel())
                                .coinId(otcCoin.getId())
                                .coinName(otcCoin.getName())
                                .advertiseId(x.getId())
                                .createTime(x.getCreateTime())
                                .advertiseType(advertiseType)
                                .build()
                ).collect(Collectors.toList()));

        return specialPage;
    }

    @Override
    public List<ScanAdvertise> getAllExcellentAdvertise(AdvertiseType type, List<Map<String, String>> list) {
        List<ScanAdvertise> excellents = new ArrayList<>();
        list.parallelStream()
                .forEachOrdered((Map<String, String> x) -> {
                    OtcCoin otcCoin = coinService.findUnitByUnitAndStatus(x.get("name"), CommonStatus.NORMAL);
                    if (otcCoin != null) {
                        try {
                            List<Map<String, String>> mapList = this.baseMapper.getPriceBySql(new BigDecimal(x.get("price")),type.getCode(),otcCoin.getId());
                            if (mapList.size() > 0) {
                                Advertise advertise = this.getById(Long.valueOf(mapList.get(0).get("advertise_id")));
                                Member member = memberFeign.findMemberById(advertise.getMemberId());
                                excellents.add(ScanAdvertise
                                        .builder()
                                        .advertiseId(advertise.getId())
                                        .coinId(otcCoin.getId())
                                        .coinName(otcCoin.getName())
                                        .coinNameCn(otcCoin.getNameCn())
                                        .createTime(advertise.getCreateTime())
                                        .maxLimit(advertise.getMaxLimit())
                                        .minLimit(advertise.getMinLimit())
                                        .memberName(member.getUsername())
                                        .avatar(member.getAvatar())
                                        .level(member.getMemberLevel())
                                        .payMode(advertise.getPayMode())
                                        .unit(otcCoin.getUnit())
                                        .remainAmount(advertise.getRemainAmount())
                                        .transactions(member.getTransactions())
                                        .price(BigDecimalUtils.round(Double.valueOf(mapList.get(0).get("minPrice")), 2))
                                        .build()
                                );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        return excellents;
    }

    @Override
    public List<ScanAdvertise> getLatestAdvertise() {
        List<ScanAdvertise> result =new ArrayList<>();
        LambdaQueryWrapper<Advertise> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Advertise::getStatus,0);
        queryWrapper.orderByDesc(Advertise::getId);
        queryWrapper.last("limit 10");
        List<Advertise> list = this.list(queryWrapper);
        if (null != list && list.size() > 0) {
            result=list.stream().map((x) ->{
                    Member member = memberFeign.findMemberById(x.getMemberId());
                    ScanAdvertise build = ScanAdvertise.builder()
                        .premiseRate(x.getPriceType() == 0 ? null : x.getPremiseRate())
                        .price(BigDecimalUtils.round(x.getPrice(), 2))
                        .transactions(x.getTransactions())
                        .remainAmount(x.getRemainAmount())
                        .unit(x.getCoinUnit())
                        .payMode(x.getPayMode())
                        .memberName(member.getUsername())
                        .avatar(member.getAvatar())
                        .minLimit(x.getMinLimit())
                        .maxLimit(x.getMaxLimit())
                        .level(member.getMemberLevel())
                        .coinId(x.getCoinId())
                        .advertiseId(x.getId())
                        .createTime(x.getCreateTime())
                        .advType(x.getAdvertiseType())
                        .build();
                    return build;
                }
            ).collect(Collectors.toList());
        }
        return result;
    }
}
