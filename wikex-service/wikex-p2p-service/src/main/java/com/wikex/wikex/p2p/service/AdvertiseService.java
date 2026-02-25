package com.wikex.wikex.p2p.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.AdvertiseControlStatus;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.p2p.entity.Advertise;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.vo.AdvertiseVo;
import com.wikex.wikex.p2p.vo.MemberAdvertiseDetail;
import com.wikex.wikex.p2p.vo.MemberAdvertiseInfo;
import com.wikex.wikex.p2p.vo.ScanAdvertise;
import com.wikex.wikex.screen.AdvertiseScreen;
import com.wikex.wikex.user.entity.Member;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface AdvertiseService extends IService<Advertise> {

    int turnOffBatch(AdvertiseControlStatus status, Long[] ids);

    Page<Advertise> findAll(AdvertiseScreen screen);

    List<Advertise> queryAdvertise(Date startTime, Date endTime, AdvertiseType advertiseType, String realName);

    List<AdvertiseVo> selectSellAutoOffShelves(Long id, BigDecimal marketPrice, BigDecimal jyRate);

    List<AdvertiseVo> selectBuyAutoOffShelves(Long id, BigDecimal marketPrice);

    void autoPutOffShelves(AdvertiseVo y, OtcCoin x) throws InformationExpiredException;

    boolean updateAdvertiseAmountForCancel(Long advertiseId, BigDecimal amount);

    boolean updateAdvertiseAmountForBuy(Long id, BigDecimal amount);

    boolean updateAdvertiseAmountForRelease(Long advertiseId, BigDecimal amount);

    Long getAdvertiserNum(Long memberId);

    MemberAdvertiseDetail findOne(Long id, Long memberId);

    Advertise modifyAdvertise(Advertise advertise, Advertise old);

    Advertise find(Long id, Long memberId);

    int putOffShelves(Long id, BigDecimal remainAmount);

    MemberAdvertiseInfo getMemberAdvertise(Member member, HashMap<String,HashMap<String,BigDecimal>> coins);

    Page<ScanAdvertise> paginationAdvertise(Integer pageNo, Integer pageSize,String country, OtcCoin otcCoin, AdvertiseType advertiseType, double marketPrice, Integer isCertified);

    List<ScanAdvertise> getAllExcellentAdvertise(AdvertiseType advertiseType, List<Map<String, String>> marketPrices);

    List<ScanAdvertise> getLatestAdvertise();
}
