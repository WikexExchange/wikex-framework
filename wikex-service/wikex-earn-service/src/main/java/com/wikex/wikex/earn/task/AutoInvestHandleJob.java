package com.wikex.wikex.earn.task;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.earn.entity.AutoInvestActivity;
import com.wikex.wikex.earn.entity.AutoInvestOrder;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.wikex.wikex.earn.service.AutoInvestActivityService;
import com.wikex.wikex.earn.service.AutoInvestOrderService;
import com.wikex.wikex.earn.service.AutoInvestPlanService;
import com.wikex.wikex.earn.service.LockedSavingsStatisticService;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AutoInvestHandleJob {
    @Autowired
    private LockedSavingsStatisticService lockedSavingsStatisticService;
	@Autowired
	private MarketFeign marketFeign;
    @Autowired
    private MemberFeign memberService;
	@Autowired
	private MemberWalletFeign memberWalletService;
    @Autowired
    private MemberTransactionFeign memberTransactionService;
	@Autowired
	private AutoInvestOrderService autoInvestOrderService;
    @Autowired
    private AutoInvestPlanService autoInvestPlanService;
	@Autowired
	private AutoInvestActivityService autoInvestActivityService;


	@XxlJob("autoInvest")
    public void autoInvest() throws ParseException {
		QueryWrapper<AutoInvestPlan> query = new QueryWrapper<>();
		query.eq("status",0);
		query.eq("del_flag",0);
		query.le("next_time",new Date());
		query.orderByDesc("create_time");
		List<AutoInvestPlan> list = autoInvestPlanService.list(query);

			if(list!=null && list.size() > 0){
			for (AutoInvestPlan plan : list) {
					AutoInvestActivity activity = autoInvestActivityService.getById(plan.getActivityId());
					if(activity==null){
							log.error("Auto-invest plan id:" + plan.getId() + " activity not found!");
							continue;
					}
					Member member = memberService.findMemberById(plan.getMemberId());
					if(member==null){
							log.error("Auto-invest plan id:" + plan.getId() + " user not found!");
							continue;
					}
					if(plan.getStatus()!=0){
							log.error("Auto-invest plan id:" + plan.getId() + " status is incorrect!");
							continue;
					}
					
					MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(plan.getBaseUnit(), member.getId());
					if(wallet==null){
							log.error("Auto-invest plan id:" + plan.getId() + " user wallet does not exist!");
							continue;
					}
					if(wallet.getBalance().compareTo(plan.getAmount()) < 0) {
							log.error("Auto-invest plan id:" + plan.getId() + " insufficient user wallet balance");
							continue;
					}

				
				
				BigDecimal lastPrice = getLastPrice(plan.getBaseUnit(), plan.getCoinUnit());
				
				MemberWallet targetMW = memberWalletService.findByCoinUnitAndMemberId(plan.getCoinUnit(), member.getId());
				BigDecimal amount = plan.getAmount().multiply(lastPrice);
				BigDecimal fee = amount.multiply(activity.getFee());
				amount = amount.subtract(fee);


				
				memberWalletService.increaseBalance(targetMW.getId(), amount);
				
				memberWalletService.deductBalance(wallet.getId(), plan.getAmount());

				
				MemberTransaction memberTransaction = new MemberTransaction();
				memberTransaction.setFee(BigDecimal.ZERO);
				memberTransaction.setAmount(amount);
				memberTransaction.setMemberId(member.getId());
				memberTransaction.setSymbol(plan.getCoinUnit());
				memberTransaction.setType(TransactionType.AUTO_INVEST_BUY.getCode());
				memberTransaction.setCreateTime(DateUtil.getCurrentDate());
				memberTransaction.setRealFee(fee.toPlainString());
				memberTransaction.setDiscountFee("0");
				memberTransactionService.save(memberTransaction);

				
				MemberTransaction memberTransactionOut = new MemberTransaction();
				memberTransactionOut.setFee(BigDecimal.ZERO);
				memberTransactionOut.setAmount(plan.getAmount());
				memberTransactionOut.setMemberId(member.getId());
				memberTransactionOut.setSymbol(plan.getBaseUnit());
				memberTransactionOut.setType(TransactionType.AUTO_INVEST_SELL.getCode());
				memberTransactionOut.setCreateTime(DateUtil.getCurrentDate());
				memberTransactionOut.setRealFee("0");
				memberTransactionOut.setDiscountFee("0");
				memberTransactionService.save(memberTransactionOut);

				
				AutoInvestOrder order = new AutoInvestOrder();
				order.setBaseNum(plan.getAmount());
				order.setPlanId(plan.getId());
				order.setCreateTime(new Date());
				order.setInvestTime(plan.getNextTime());
				order.setBaseUnit(plan.getBaseUnit());
				order.setMemberId(plan.getMemberId());
				order.setCoinUnit(plan.getCoinUnit());
				order.setFee(fee);
				order.setNum(amount);
				order.setStatus(0);
				order.setUpdateTime(new Date());
				autoInvestOrderService.save(order);

				
				Date nextTime = null;
				int cycle = plan.getCycle();
				String startTime = plan.getStartTime();
				if(cycle==0){
					String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(plan.getNextTime(), -1))+" "+startTime;
					nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
				}else if(cycle==1){
					String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -7))+" "+startTime;
					nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
				}else if(cycle==2){
					String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -30))+" "+startTime;
					nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
				}
				if(nextTime!=null){
					plan.setNextTime(nextTime);
				}
				plan.setUpdateTime(new Date());
				plan.setCumulativeBuyAmount(plan.getCumulativeBuyAmount().add(amount));
				plan.setCumulativeAmount(plan.getCumulativeAmount().add(plan.getAmount()));
				plan.setAveragePrice(plan.getCumulativeAmount().divide(plan.getCumulativeBuyAmount(),RoundingMode.HALF_UP));
				autoInvestPlanService.saveOrUpdate(plan);

			}
		}

    }


	private BigDecimal getLastPrice(String fromUnit,String toUnit){
		if(fromUnit.equalsIgnoreCase(toUnit)){
			return BigDecimal.ONE;
		}
		List<CoinThumb> thumbList =marketFeign.findSymbolThumb4Feign();
		String symbol=fromUnit.toUpperCase()+"/"+toUnit.toUpperCase();
		String symbol2 = toUnit.toUpperCase()+"/"+fromUnit.toUpperCase();
		for (CoinThumb coinThumb : thumbList) {
			if(coinThumb.getSymbol().equalsIgnoreCase(symbol)){
				return coinThumb.getClose();
			}
			if(coinThumb.getSymbol().equalsIgnoreCase(symbol2)){
				return BigDecimal.ONE.divide(coinThumb.getClose(),8, RoundingMode.HALF_UP);
			}
		}
		return BigDecimal.ONE;
	}

}
