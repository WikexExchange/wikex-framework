package com.wikex.wikex.p2p.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.AppealStatus;
import com.wikex.wikex.p2p.entity.Appeal;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.mapper.AppealMapper;
import com.wikex.wikex.p2p.service.AppealService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.p2p.service.OtcOrderService;
import com.wikex.wikex.p2p.vo.AppealVo;
import com.wikex.wikex.screen.AppealScreen;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {

    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private OtcOrderService otcOrderService;
    @Autowired
    private OtcCoinService otcCoinService;

    @Override
    public Page appealQuery(AppealScreen screen) {
        Page<AppealVo> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        Page<AppealVo> appealVoPage = this.baseMapper.appealQuery(page, screen);
        appealVoPage.getRecords().stream().map(
                e->{
                    if(e.getInitiatorId().equals(e.getMemberId())){
                        e.setInitiatorName(e.getAdvertiseCreaterName());
                        e.setInitiatorUsername(e.getAdvertiseCreaterUserName());
                        e.setAssociateName(e.getCustomerName());
                        e.setAssociateUsername(e.getCustomerUserName());
                    }else{
                        e.setInitiatorName(e.getCustomerName());
                        e.setInitiatorUsername(e.getCustomerUserName());
                        e.setAssociateName(e.getAdvertiseCreaterName());
                        e.setAssociateUsername(e.getAdvertiseCreaterUserName());
                    }
                    return e;
                }
        ).collect(Collectors.toList());
        return appealVoPage;
    }

    @Override
    public AppealVo findOneAppealVO(Long id) {
        return generateAppealVO(getById(id));
    }

    @Override
    public Appeal findOne(Long id) {
        return getById(id);
    }

    @Override
    public Integer countAuditing() {
        LambdaQueryWrapper<Appeal> query = new LambdaQueryWrapper<>();
        query.eq(Appeal::getStatus, AppealStatus.NOT_PROCESSED);
        return this.count(query);
    }

    
    private AppealVo generateAppealVO(Appeal appeal){
        Member initialMember = memberFeign.findMemberById(appeal.getInitiatorId());
        Member associateMember = memberFeign.findMemberById(appeal.getAssociateId());
        AppealVo vo = new AppealVo();
        vo.setAppealId(appeal.getId());
        vo.setAssociateName(associateMember.getRealName());
        vo.setAssociateUsername(associateMember.getUsername());
        vo.setInitiatorName(initialMember.getRealName());
        vo.setInitiatorUsername(initialMember.getUsername());
        OtcOrder order = otcOrderService.getById(appeal.getOrderId());
        OtcCoin otcCoin = otcCoinService.getById(order.getCoinId());
        vo.setCoinName(otcCoin.getName());
        vo.setFee(order.getCommission());
        vo.setMoney(order.getMoney());
        vo.setOrderSn(order.getOrderSn());
        vo.setNumber(order.getNumber());
        vo.setOrderStatus(order.getStatus());
        vo.setPayMode(order.getPayMode());
        vo.setTransactionTime(order.getCreateTime());
        vo.setIsSuccess(appeal.getIsSuccess());
        vo.setAdvertiseType(order.getAdvertiseType());
        vo.setAdvertiseCreaterName(order.getMemberRealName());
        vo.setAdvertiseCreaterUserName(order.getMemberName());
        vo.setCustomerUserName(order.getCustomerName());
        vo.setCustomerName(order.getCustomerRealName());
        vo.setStatus(appeal.getStatus());
        vo.setCreateTime(appeal.getCreateTime());
        vo.setDealWithTime(appeal.getDealWithTime());
        vo.setRemark(appeal.getRemark());
        return vo ;
    }
}
