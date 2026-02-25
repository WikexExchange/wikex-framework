package com.wikex.wikex.earn.task;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.wikex.wikex.earn.entity.LockedSavingsOrder;
import com.wikex.wikex.earn.entity.LockedSavingsStatistic;
import com.wikex.wikex.earn.service.LockedSavingsOrderService;
import com.wikex.wikex.earn.service.LockedSavingsStatisticService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class LockedOrderHandleJob {
    @Autowired
    private LockedSavingsStatisticService lockedSavingsStatisticService;

    @Autowired
    private MemberFeign memberService;
	@Autowired
	private MemberWalletFeign memberWalletService;
    @Autowired
    private MemberTransactionFeign memberTransactionService;

    @Autowired
    private LockedSavingsOrderService lockedSavingsOrderService;


    /**
     * Process matured fixed-term savings at 2:30 AM every day.
     */
    //    @Scheduled(cron = "0 30 2 * * *")
	@XxlJob("lockedOrderHandle")
    public void lockedOrderHandle() {

		QueryWrapper<LockedSavingsOrder> query = new QueryWrapper<>();
		query.eq("status",0);
		query.le("end_time",new Date());
		query.orderByDesc("create_time");
		List<LockedSavingsOrder> list = lockedSavingsOrderService.list(query);


		if(list!=null && list.size() > 0){
			for (LockedSavingsOrder order : list) {
				Member member = memberService.findMemberById(order.getMemberId());
				if(member==null){
					log.error("Fixed-term finance order id:" + order.getId() + " user not found!");
					continue;
				}
				if(order.getStatus()!=0){
					log.error("Fixed-term finance order id:" + order.getId() + " status is incorrect!");
					continue;
				}
				// Get wallet
				MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getCoinUnit(), member.getId());
				if(wallet==null){
					log.error("Fixed-term finance order id:" + order.getId() + " user wallet does not exist!");
					continue;
				}
				order.setStatus(1);
				order.setUpdateTime(new Date());
				lockedSavingsOrderService.saveOrUpdate(order);
				BigDecimal fee = BigDecimal.ZERO;
				// Unfreeze assets
				memberWalletService.thawBalance(order.getCoinUnit(),member.getId(), order.getNum());
				// Add profit record
				MemberTransaction transaction = new MemberTransaction();
				transaction.setFee(BigDecimal.ZERO);
				transaction.setAmount(order.getNum());
				transaction.setSymbol(order.getCoinUnit());
				transaction.setType(TransactionType.LOCKED_SAVING_SELL.getCode());
				transaction.setMemberId(order.getMemberId());
				transaction.setRealFee("0");
				transaction.setDiscountFee("0");
				transaction.setCreateTime(new Date());
				memberTransactionService.save(transaction);
				// Add earnings to assets
				if (order.getEarnNum().compareTo(BigDecimal.ZERO) == 1) {
					memberWalletService.increaseBalance(wallet.getId(), order.getEarnNum());
					// Add profit record
					MemberTransaction memberTransaction = new MemberTransaction();
					memberTransaction.setFee(BigDecimal.ZERO);
					memberTransaction.setAmount(order.getEarnNum());
					memberTransaction.setSymbol(order.getCoinUnit());
					memberTransaction.setType(TransactionType.FINANCE_REWARD.getCode());
					memberTransaction.setMemberId(order.getMemberId());
					memberTransaction.setRealFee("0");
					memberTransaction.setDiscountFee("0");
					memberTransaction.setCreateTime(new Date());
					memberTransactionService.save(memberTransaction);
				}
				// Statistics: decrease amount and increase earnings
				LockedSavingsStatistic statistic = lockedSavingsStatisticService.findByMemberIdAndCoinSymbol(member.getId(), order.getCoinUnit());
				lockedSavingsStatisticService.decreaseNumAndIncreaseEarnNum(statistic.getId(), order.getNum(),order.getEarnNum());
			}
		}

    }



}
