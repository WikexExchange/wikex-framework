package com.wikex.wikex.admin.task;

import com.wikex.wikex.active.entity.MiningOrder;
import com.wikex.wikex.active.entity.MiningOrderDetail;
import com.wikex.wikex.active.feign.MiningOrderDetailFeign;
import com.wikex.wikex.active.feign.MiningOrderFeign;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class MiningsJob {

//	@Autowired
//    private SMSProvider smsProvider;

	@Autowired
	private MiningOrderDetailFeign miningOrderDetailFeign;

	@Autowired
	private MiningOrderFeign miningOrderFeign;

	@Autowired
	private MemberWalletFeign memberWalletFeign;

	@Autowired
	private MemberTransactionFeign memberTransactionFeign;

//	@Autowired
//	private MemberFeign memberFeign;

	/**
	 * Daily payout of miner profits at 10:30 PM
	 */
	//	@Scheduled(cron = "0 30 22 * * *")
    @XxlJob("minings")
	public void minings() {
		List<MiningOrder> list = miningOrderFeign.findAllByMiningStatus(1);

		Date currentDate = DateUtil.getCurrentDate();
		for(MiningOrder item : list) {
			if(currentDate.compareTo(item.getEndTime()) < 0) {
//				Member member = memberFeign.findMemberById(item.getMemberId());
				// Generate profit
				MemberWallet userWallet = memberWalletFeign.findByCoinUnitAndMemberId(item.getMiningUnit(), item.getMemberId());
				if(userWallet != null) {
					// Transaction record
					MemberTransaction memberTransaction1 = new MemberTransaction();
					memberTransaction1.setFee(BigDecimal.ZERO);
					memberTransaction1.setAmount(item.getCurrentDaysprofit());
					memberTransaction1.setMemberId(item.getMemberId());
			        memberTransaction1.setSymbol(item.getMiningUnit());
			        memberTransaction1.setType(TransactionType.ACTIVITY_BUY.getCode());
			        memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
			        memberTransaction1.setRealFee("0");
			        memberTransaction1.setDiscountFee("0");
			        memberTransactionFeign.save(memberTransaction1);
			        // Update balance
			        userWallet.setBalance(userWallet.getBalance().add(item.getCurrentDaysprofit()));
			        memberWalletFeign.save(userWallet);

			        // Update miner data
			        item.setTotalProfit(item.getTotalProfit().add(item.getCurrentDaysprofit()));
			        item.setMiningedDays(item.getMiningedDays() + 1);
			        miningOrderFeign.save(item);

			        // Add miner output data
			        MiningOrderDetail detail = new MiningOrderDetail();
			        detail.setMemberId(item.getMemberId());
			        detail.setCreateTime(DateUtil.getCurrentDate());
			        detail.setMiningOrderId(item.getId());
			        detail.setMiningUnit(item.getMiningUnit());
			        detail.setOutput(item.getCurrentDaysprofit());

					miningOrderDetailFeign.save(detail);

			        // Send SMS notification to user
//			        try {
//		 				smsProvider.sendCustomMessage(member.getMobilePhone(), "Dear user, your mining machine 【"+ item.getTitle() + "】produced today: " + item.getCurrentDaysprofit() + " "+item.getMiningUnit() + ", please check your account!");
//		 			} catch (Exception e) {
//		 				e.printStackTrace();
//		 			}
				}
			}
		}
	}
}
