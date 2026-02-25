package com.wikex.wikex.active.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.active.entity.Activity;
import com.wikex.wikex.active.entity.ActivityOrder;
import com.wikex.wikex.active.mapper.ActivityOrderMapper;
import com.wikex.wikex.active.service.ActivityOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.active.service.ActivityService;
import com.wikex.wikex.exception.WikexRuntimeException;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  Service Implementation
 * </p>
 *
 * @author markchao
 * @since 2021-08-18
 */
@Service
public class ActivityOrderServiceImpl extends ServiceImpl<ActivityOrderMapper, ActivityOrder> implements ActivityOrderService {

    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private ActivityService activityService;

    @Override
    public IPage<ActivityOrder> finaAllByMemberId(Long memberId, Integer pageNo, Integer pageSize) {
        Page<ActivityOrder> page = new Page<>(pageNo,pageSize);
        QueryWrapper<ActivityOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        IPage<ActivityOrder> activityOrderPage = this.baseMapper.selectPage(page, queryWrapper);
        return activityOrderPage;
    }

    @Override
    public List<ActivityOrder> findAllByActivityIdAndMemberId(Long memberId, Long activityId) {
        QueryWrapper<ActivityOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId).eq("activity_id",activityId);
        return this.list(queryWrapper);
    }


    @Override
    public MessageResult saveActivityOrder(ActivityOrder activityOrder) throws WikexRuntimeException {
        MemberWallet wallet = memberWalletFeign.findByCoinUnitAndMemberId(activityOrder.getBaseSymbol(), activityOrder.getMemberId());
        if(wallet.getIsLock().equals(BooleanEnum.IS_TRUE)){
            return MessageResult.error(msService.getMessage("WALLET_LOCKED"));
        }
        // Freeze assets
        MessageResult result = memberWalletFeign.freezeBalance(wallet.getId(), activityOrder.getTurnover());
        if (result.getCode() != 0) {
            throw new WikexRuntimeException("UNABLE_TO_LOCK_ASSET");
        }
        // Update Activity participation info
        Activity activity = activityService.getById(activityOrder.getActivityId());
        if (activity == null) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ACTIVITIES"));
        }
        if(activity.getType() == 3) { // Holding-based distribution: update frozen asset amount
            activity.setFreezeAmount(activity.getFreezeAmount().add(activityOrder.getFreezeAmount()));
        }else if(activity.getType() == 4){ // Open subscription: update traded amount
            activity.setTradedAmount(activity.getTradedAmount().add(activityOrder.getAmount()));
        }else if(activity.getType() == 5){ // Mining machine subscription: update traded amount
            activity.setTradedAmount(activity.getTradedAmount().add(activityOrder.getAmount()));
        }else if(activity.getType() == 6) { // Locked subscription
            activity.setTradedAmount(activity.getTradedAmount().add(activityOrder.getAmount()));
        }

        // Update progress
        if(activity.getType() == 4 || activity.getType() == 5) {
            // New progress must not be less than old progress
            int newProgress = activity.getTradedAmount().divide(activity.getTotalSupply()).multiply(new BigDecimal(100)).intValue();
            activity.setProgress(newProgress >= activity.getProgress() ? newProgress : activity.getProgress());
        }
        // Update Activity table
        Boolean saveResult = activityService.updateById(activity);
        if(!saveResult) {
            throw new WikexRuntimeException("UPDATE_ACTIVITY_FAILED");
        }
        Boolean ok = this.saveOrUpdate(activityOrder);
        if (ok) {
            return MessageResult.success("success");
        } else {
            throw new WikexRuntimeException("error");
        }
    }

    @Override
    public List<ActivityOrder> findAllByActivityId(Long activityId) {
        QueryWrapper<ActivityOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id",activityId);
        return this.list(queryWrapper);
    }
}
