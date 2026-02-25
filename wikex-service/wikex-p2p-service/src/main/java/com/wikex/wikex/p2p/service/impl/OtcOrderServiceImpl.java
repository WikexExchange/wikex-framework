package com.wikex.wikex.p2p.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.constant.OrderStatus;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.mapper.OtcOrderMapper;
import com.wikex.wikex.p2p.service.AdvertiseService;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.p2p.service.OtcOrderService;
import com.wikex.wikex.screen.OrderScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.IdWorkByTwitter;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.OtcOrderVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wikex.wikex.util.BigDecimalUtils.add;


@Service
public class OtcOrderServiceImpl extends ServiceImpl<OtcOrderMapper, OtcOrder> implements OtcOrderService {

    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;
    @Autowired
    private LocaleMessageSourceService messageSourceService;

    @Override
    public List<OtcOrder> checkExpiredOrder() {
        return this.baseMapper.findAllExpiredOrder(new Date());
    }

    @Override
    @Transactional
    public void cancelOrderTask(OtcOrder order) throws InformationExpiredException {

        if (order.getAdvertiseType().equals(AdvertiseType.BUY)) {
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException("Information Expired");
            }
            OtcCoin otcCoin = otcCoinService.getById(order.getCoinId());
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), order.getCustomerId());
            MessageResult result = memberWalletService.thawBalance(memberWallet.getCoinId(),memberWallet.getMemberId(), order.getNumber());
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException("Information Expired");
            }
            OtcCoin otcCoin = otcCoinService.getById(order.getCoinId());
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), order.getCustomerId());
            MessageResult result = memberWalletService.thawBalance(memberWallet.getCoinId(),memberWallet.getMemberId(), add(order.getNumber(), order.getCommission()));
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        }
        if (!(this.cancelOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
    }

    
    @Override
    public int cancelOrder(String orderSn) {
        return this.baseMapper.cancelOrder(new Date(), OrderStatus.CANCELLED.getCode(), orderSn);
    }

    @Override
    public OtcOrder saveOrder(OtcOrder order) {
        order.setOrderSn(String.valueOf(idWorkByTwitter.nextId()));
        this.saveOrUpdate(order);
        return order;
    }

    @Override
    public Page<OtcOrder> pageQuery(int pageNo, int pageSize, OrderStatus status, long id, String orderSn) {
        Page<OtcOrder> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<OtcOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper->wrapper.eq(OtcOrder::getMemberId,id).or().eq(OtcOrder::getCustomerId,id));
        queryWrapper.eq(OtcOrder::getStatus,status.getCode());
        if (StringUtils.isNotBlank(orderSn)) {
            queryWrapper.like(OtcOrder::getOrderSn,orderSn);
        }
        queryWrapper.orderByDesc(OtcOrder::getId);
        return this.page(page,queryWrapper);
    }

    @Override
    public OtcOrder findOneByOrderSn(String orderSn) {
        LambdaQueryWrapper<OtcOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OtcOrder::getOrderSn,orderSn);
        return this.getOne(queryWrapper);
    }

    @Override
    public int payForOrder(String orderSn) {
        return this.baseMapper.payForOrder(new Date(), OrderStatus.PAID.getCode(), orderSn);
    }

    @Override
    public int releaseOrder(String orderSn) {
        return this.baseMapper.releaseOrder(new Date(), OrderStatus.COMPLETED.getCode(), orderSn);
    }

    @Override
    public int updateOrderAppeal(String orderSn) {
        return this.baseMapper.updateAppealOrder(OrderStatus.APPEAL.getCode(), orderSn);
    }

    @Override
    public Page<OtcOrderVO> outExcel(OrderScreen screen) {
        Page<OtcOrderVO> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        if(screen.getEndTime()!=null){
            screen.setEndTime(DateUtil.dateAddDay(screen.getEndTime(),1));
        }
        return this.baseMapper.outExcel(page,screen);
    }

    @Override
    public MessageResult getOrderNum() {
        QueryWrapper<OtcOrder> query = new QueryWrapper<>();
        query.eq("status",OrderStatus.NONPAYMENT.getCode());
        int noPayNum = this.count(query);
        query = new QueryWrapper<>();
        query.eq("status",OrderStatus.PAID.getCode());
        int paidNum = this.count(query);
        query = new QueryWrapper<>();
        query.eq("status",OrderStatus.COMPLETED.getCode());
        int finishedNum = this.count(query);
        query = new QueryWrapper<>();
        query.eq("status",OrderStatus.CANCELLED.getCode());
        int cancelNum = this.count(query);
        query = new QueryWrapper<>();
        query.eq("status",OrderStatus.APPEAL.getCode());
        int appealNum = this.count(query);
        Map<String, Integer> map = new HashMap<>();
        map.put("noPayNum", noPayNum);
        map.put("paidNum", paidNum);
        map.put("finishedNum", finishedNum);
        map.put("cancelNum", cancelNum);
        map.put("appealNum", appealNum);
        return MessageResult.getSuccessInstance(messageSourceService.getMessage("GET_SUCCESS"), map);
    }

    @Override
    public List<OtcOrderVO> getOtcOrderStatistics(String dateStr) {
        return this.baseMapper.getOtcOrderStatistics(dateStr);
    }
}
