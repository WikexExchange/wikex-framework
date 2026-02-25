package com.wikex.wikex.admin.task;

import com.wikex.wikex.active.entity.LockedOrder;
import com.wikex.wikex.active.entity.LockedOrderDetail;
import com.wikex.wikex.active.feign.LockedOrderDetailFeign;
import com.wikex.wikex.active.feign.LockedOrderFeign;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class LockedReleaseJob {


    private static Logger logger = LoggerFactory.getLogger(LockedReleaseJob.class);

    @Autowired
    private LockedOrderDetailFeign lockedOrderDetailFeign;

    @Autowired
    private LockedOrderFeign lockedOrderFeign;

    @Autowired
    private MemberWalletFeign memberWalletFeign;

    @Autowired
    private MemberTransactionFeign memberTransactionFeign;


    private void increaseToRelease(){

    }
    /**
     * Daily process: Release locked positions every night at 11:30 PM
     */
    //@Scheduled(cron = "0 30 23 * * *")
    //@Transactional(rollbackFor = Exception.class)
    @XxlJob("LockedReleaseJob")
    public void release() {
        List<LockedOrder> list = lockedOrderFeign.findAllByLockedStatus(1);

        Date currentDate = DateUtil.getCurrentDate();
        for(LockedOrder item : list) {
            if(this.checkNuccessary(item)) {
                this.doRelease(item);
            }
        }
    }

    private boolean checkNuccessary(LockedOrder item){
        if (item.getLockedStatus() != 1) { // Not in releasing status
            return false;
        }
        // Daily cycle, release directly
        // Weekly cycle, calculate whether it is release cycle
        long days = DateUtil.diffDays(item.getCreateTime(), DateUtil.getCurrentDate());
        if(days == 0) {
            return false;
        }
        if (item.getPeriod() == 1) {
            if (days % 7 != 0) return false;
        }
        // Monthly cycle, calculate whether it is release cycle (release once every 30 days)
        if (item.getPeriod() == 2) {
            if (days % 30 != 0) return false;
        }
        // Annual cycle, calculate whether it is release cycle (release once every 365 days)
        if (item.getPeriod() == 3) {
            if (days % 365 != 0) return false;
        }
        return true;
    }

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void doRelease(LockedOrder item){

        // Get current wallet
        MemberWallet userWallet = memberWalletFeign.findByCoinUnitAndMemberId(item.getReleaseUnit(), item.getMemberId());

        LockedOrderDetail lod = new LockedOrderDetail();
        BigDecimal rAmount = item.getCurrentReleaseamount(); // Equal-amount release
        if (item.getReleaseType() == 1) { // Proportional release
            rAmount = item.getTotalLocked().subtract(item.getTotalRelease()).multiply(item.getReleaseCurrentpercent()); // Remaining locked amount x release ratio
        }
        if (item.getLockedDays() - item.getReleasedDays() == 1) { // Last release cycle
            rAmount = item.getTotalLocked().subtract(item.getTotalRelease()); // Release all remaining locked amount
        }
        // Save release details
        lod.setOutput(rAmount);
        lod.setReleaseUnit(item.getReleaseUnit());
        lod.setMemberId(item.getMemberId());
        lod.setLockedOrderId(item.getId());
        lod.setCreateTime(DateUtil.getCurrentDate());
        lockedOrderDetailFeign.save(lod);

        // Deduct assets from user wallet table
        if(userWallet == null) {
            logger.info("=======>userWallet is null");
        }
        if(rAmount == null) {
            logger.info("=======>rAmount is null");
        }
        memberWalletFeign.decreaseToRelease(userWallet.getId(), rAmount);
        // Increase balance assets
        memberWalletFeign.increaseBalance(userWallet.getId(), rAmount);
        // Add asset change record
        MemberTransaction memberTransaction1 = new MemberTransaction();
        memberTransaction1.setFee(BigDecimal.ZERO);
        memberTransaction1.setAmount(rAmount);
        memberTransaction1.setMemberId(item.getMemberId());
        memberTransaction1.setSymbol(item.getReleaseUnit());
        memberTransaction1.setType(TransactionType.ACTIVITY_BUY.getCode());
        memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction1.setRealFee("0");
        memberTransaction1.setDiscountFee("0");
        memberTransactionFeign.save(memberTransaction1);
        // Update main table
        item.setTotalRelease(item.getTotalRelease().add(rAmount));
        item.setReleasedDays(item.getReleasedDays() + 1);
        if (item.getLockedDays() - item.getReleasedDays() == 0) { // Last release cycle
            item.setLockedStatus(2); // Ended
        }
        lockedOrderFeign.save(item);
    }
}
