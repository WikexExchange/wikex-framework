package com.wikex.wikex.earn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.earn.entity.*;
import com.wikex.wikex.earn.service.*;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.earn.vo.LockedSavingAddVo;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(tags = "Wealth Management")
@RestController
//@RequestMapping("/earn")
public class EarnController extends BaseController {
    private Logger log = LoggerFactory.getLogger(EarnController.class);

    @Autowired
    private LockedSavingsActivityService lockedSavingsActivityService;
    @Autowired
    private LockedSavingsOrderService lockedSavingsOrderService;
    @Autowired
    private MemberFeign memberService;
    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private LockedSavingsStatisticService lockedSavingsStatisticService;
    @Autowired
    private AutoInvestActivityService autoInvestActivityService;
    @Autowired
    private AutoInvestPlanService autoInvestPlanService;
    @Autowired
    private AutoInvestOrderService autoInvestOrderService;
    @Autowired
    private MemberTransactionFeign memberTransactionService;
    @Autowired
    private MarketFeign marketFeign;


    // Get locked-savings activities
    @ApiOperation(value = "Get Locked-Savings Activities")
    @RequestMapping("locked/activity/list")
    public MessageResult lockedActivity(ActivityParam pageParam) {

        IPage<LockedSavingsActivity> all = lockedSavingsActivityService.findAll(pageParam);
        if (all != null && all.getRecords() != null) {
            for (LockedSavingsActivity activity : all.getRecords()) {
                String day = msService.getMessage("EARN_DAY");
                String n = msService.getMessage("EARN_LOCKED");
                if (StringUtils.isEmpty(day)) {
                    day = "days";
                }
                if (StringUtils.isEmpty(n)) {
                    n = "LockedSavings";
                }
                String name = activity.getCoinUnit().toUpperCase() + "(" + activity.getDuration() + day + ")" + n;
                activity.setName(name);
            }
        }
        return success(IPage2Page(all));
    }



    // Get locked-savings activity details by id
    @ApiOperation(value = "Get Locked-Savings Activity Details by ID")
    @RequestMapping("locked/activity/Detail")
    public MessageResult lockedActivityDetail(Long id) {
        LockedSavingsActivity activity = lockedSavingsActivityService.getById(id);
        return success(activity);
    }

    @ApiOperation(value = "Get Ongoing Locked-Savings Orders")
    @PermissionOperation
    @RequestMapping("locked/going/order")
    public MessageResult lockedGoingOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, PageParam pageParam) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        IPage<LockedSavingsOrder> all = lockedSavingsOrderService.lockedGoingOrder(member.getId(), pageParam);
        return success(IPage2Page(all));
    }

    @ApiOperation(value = "Get Completed Locked-Savings Orders")
    @PermissionOperation
    @RequestMapping("locked/done/order")
    public MessageResult lockedDoneOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, PageParam pageParam) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        IPage<LockedSavingsOrder> all = lockedSavingsOrderService.lockedDoneOrder(member.getId(), pageParam);
        return success(IPage2Page(all));
    }

    /**
     * Create locked-savings order
     * @return
     */
    @ApiOperation(value = "Create Locked-Savings Order")
    @RequestMapping("locked/add")
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult lockedAddOrder(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            LockedSavingAddVo addVo
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.notNull(addVo.getAmount(), msService.getMessage("INPUT_AMOUNT"));

        Member member = memberService.findMemberById(user.getId());
        Assert.notNull(member, msService.getMessage("USER_DOES_NOT_EXIST"));
        // Whether user is prohibited from trading
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())) {
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }
        // Get activity
        LockedSavingsActivity activity = lockedSavingsActivityService.getById(addVo.getAid());
        Assert.notNull(activity, msService.getMessage("ACTIVITY_NOT_FOUND"));
        if (activity.getStatus() == 0) {
            return MessageResult.error(500, msService.getMessage(msService.getMessage("INCORRECT_ACTIVITY_STATUS")));
        }
        if (activity.getNum().compareTo(addVo.getAmount()) == 1) {
            return MessageResult.error(500, msService.getMessage(msService.getMessage("INVESTMENT_AMOUNT_CANNOT_BE_LESS_THAN_MINIMUM")));
        }

        // Get wallet
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(activity.getCoinUnit(), member.getId());
        if (wallet == null) {
            return MessageResult.error(500, msService.getMessage("BALANCE_NOT_ENOUGH"));
        }

        BigDecimal fee = BigDecimal.ZERO;
        // Requires investment amount + fee
        BigDecimal totalAmount = addVo.getAmount().add(fee);
        if (totalAmount.compareTo(wallet.getBalance()) > 0) {
            return MessageResult.error(500, msService.getMessage("BALANCE_NOT_ENOUGH"));
        }

        // Create new order
        LockedSavingsOrder orderObj = new LockedSavingsOrder();
        orderObj.setNum(addVo.getAmount());
        orderObj.setMemberId(member.getId());
        orderObj.setLockedId(addVo.getAid());
        orderObj.setRate(activity.getRate());
        BigDecimal earn = activity.getRate().divide(BigDecimal.valueOf(365), RoundingMode.HALF_UP).multiply(addVo.getAmount()).multiply(BigDecimal.valueOf(activity.getDuration()));
        orderObj.setEarnNum(earn);
        orderObj.setStatus(0);
        orderObj.setCreateTime(new Date());
        orderObj.setUpdateTime(new Date());
        orderObj.setStartTime(DateUtil.getDate(new Date(), -1));
        orderObj.setEndTime(DateUtil.getDate(new Date(), (-1 - activity.getDuration())));
        orderObj.setCoinUnit(activity.getCoinUnit());
        orderObj.setDuration(activity.getDuration());

        lockedSavingsOrderService.save(orderObj);
        // Lock assets
        memberWalletService.freezeBalance(wallet.getId(), addVo.getAmount().add(fee));

        // Add transaction record
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(orderObj.getNum());
        memberTransaction.setSymbol(orderObj.getCoinUnit());
        memberTransaction.setType(TransactionType.LOCKED_SAVING_BUY.getCode());
        memberTransaction.setMemberId(orderObj.getMemberId());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransaction.setCreateTime(new Date());
        memberTransactionService.save(memberTransaction);

        // Increase statistics
        LockedSavingsStatistic statistic = lockedSavingsStatisticService.findByMemberIdAndCoinSymbol(member.getId(), activity.getCoinUnit());
        lockedSavingsStatisticService.increaseNum(statistic.getId(), addVo.getAmount());

        return MessageResult.success("success");

    }

    @ApiOperation(value = "Locked-Savings Asset Info")
    @PermissionOperation
    @RequestMapping("locked/assets/info")
    public MessageResult lockedAssetsInfo(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        List<LockedSavingsStatistic> all = lockedSavingsStatisticService.findAll(member.getId());
        return success(all);
    }

    @ApiOperation(value = "Locked-Savings Asset Totals")
    @PermissionOperation
    @RequestMapping("locked/assets/total")
    public MessageResult lockedAssetsTotal(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        List<LockedSavingsStatistic> all = lockedSavingsStatisticService.findAll(member.getId());
        Map<String, BigDecimal> map = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal earn = BigDecimal.ZERO;
        BigDecimal totalBtc = BigDecimal.ZERO;
        for (LockedSavingsStatistic rec : all) {
            BigDecimal usdt = getLastPrice(rec.getCoinSymbol(), "USDT");
            total = total.add(usdt.multiply(rec.getNum()));
            earn = earn.add(usdt.multiply(rec.getEarnNum()));
        }

        if (total.compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal btc = getLastPrice("BTC", "USDT");
            totalBtc = total.divide(btc, 8, RoundingMode.HALF_UP);
        }
        map.put("total", total);
        map.put("totalBtc", totalBtc);
        map.put("earn", earn);
        return success(map);
    }


    // Get auto-invest activities
    @ApiOperation(value = "Get Auto-Invest Activities")
    @RequestMapping("autoInvest/activity/list")
    public MessageResult autoInvestActivity(ActivityParam pageParam) {

        IPage<AutoInvestActivity> all = autoInvestActivityService.findAll(pageParam);
        if (all.getRecords() != null) {
            for (AutoInvestActivity activity : all.getRecords()) {
                activity.setPrice(getLastPrice(activity.getCoinUnit(), activity.getBaseUnit()));
            }
        }
        return success(IPage2Page(all));
    }

    // Get auto-invest activity details by id
    @ApiOperation(value = "Get Auto-Invest Activity Details by ID")
    @RequestMapping("autoInvest/activity/Detail")
    public MessageResult autoInvestActivityDetail(Long id) {
        AutoInvestActivity activity = autoInvestActivityService.getById(id);
        return success(activity);
    }




    /**
     * Get plan list
     * @param authMember
     * @param pageParam
     * @return
     */
    @ApiOperation(value = "Get Plan List")
    @PermissionOperation
    @RequestMapping("autoInvest/plan/list")
    public MessageResult autoInvestPlan(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, PageParam pageParam) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");

        IPage<AutoInvestPlan> all = autoInvestPlanService.findAll(member.getId(), pageParam);
        if (all != null && all.getRecords() != null) {
            for (AutoInvestPlan investPlan : all.getRecords()) {
                BigDecimal lastPrice = getLastPrice(investPlan.getCoinUnit(), investPlan.getBaseUnit());
                BigDecimal profitLoss = investPlan.getCumulativeBuyAmount().multiply(lastPrice.subtract(investPlan.getAveragePrice()));
                investPlan.setProfitLoss(profitLoss.setScale(4, RoundingMode.HALF_UP).toPlainString());
                if (investPlan.getCumulativeAmount().compareTo(BigDecimal.ZERO) == 0) {
                    investPlan.setRoi("0%");
                } else {
                    investPlan.setRoi(profitLoss.divide(investPlan.getCumulativeAmount(), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP).toPlainString() + "%");
                }
            }
        }
        return success(IPage2Page(all));
    }

    /**
     * Get auto-invest orders
     * @param authMember
     * @param pageParam
     * @return
     */
    @ApiOperation(value = "Get Auto-Invest Orders")
    @PermissionOperation
    @RequestMapping("autoInvest/order/list")
    public MessageResult autoInvestOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, PageParam pageParam) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");

        IPage<AutoInvestOrder> all = autoInvestOrderService.findAll(member.getId(), pageParam);
        return success(IPage2Page(all));
    }

    /**
     * Create auto-invest plan
     * @return
     */
    @ApiOperation(value = "Create Auto-Invest Plan")
    @PermissionOperation
    @RequestMapping("autoInvest/plan/add")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addPlan(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long aid, // activity id
            String startTime, // start time
            Integer cycle, // cycle 0 daily 1 weekly 2 monthly
            BigDecimal amount // participation amount
    ) throws ParseException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.notNull(amount, msService.getMessage("INPUT_AMOUNT"));

        Member member = memberService.findMemberById(user.getId());
        Assert.notNull(member, msService.getMessage("USER_DOES_NOT_EXIST"));
        // Whether user is prohibited from trading
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())) {
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }
        // Get activity
        AutoInvestActivity activity = autoInvestActivityService.getById(aid);
        Assert.notNull(activity, msService.getMessage("ACTIVITY_NOT_FOUND"));
        if (activity.getStatus() == 0) {
            return MessageResult.error(500, msService.getMessage(msService.getMessage("INCORRECT_ACTIVITY_STATUS")));
        }
        if (activity.getNum().compareTo(amount) == 1) {
            return MessageResult.error(500, msService.getMessage(msService.getMessage("INVESTMENT_AMOUNT_CANNOT_BE_LESS_THAN_MINIMUM")));
        }

        BigDecimal fee = BigDecimal.ZERO;

        // Create new plan
        AutoInvestPlan plan = new AutoInvestPlan();
        plan.setAmount(amount);
        plan.setCoinUnit(activity.getCoinUnit());
        plan.setActivityId(aid);
        plan.setCreateTime(new Date());
        plan.setBaseUnit(activity.getBaseUnit());
        plan.setAveragePrice(BigDecimal.ZERO);
        plan.setStatus(0);
        plan.setMemberId(member.getId());
        plan.setCumulativeAmount(BigDecimal.ZERO);
        plan.setCumulativeBuyAmount(BigDecimal.ZERO);
        plan.setDelFlag(0);
        plan.setStartTime(startTime);
        // Calculate next execution time
        Date nextTime = null;
        if (cycle.intValue() == 0) { // daily
            String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -1)) + " " + startTime;
            nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
        } else if (cycle.intValue() == 1) { // weekly
            String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -8)) + " " + startTime;
            nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
        } else if (cycle.intValue() == 2) { // monthly
            String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -31)) + " " + startTime;
            nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
        }
        if (nextTime != null) {
            plan.setNextTime(nextTime);
        }
        plan.setCycle(cycle);
        plan.setUpdateTime(new Date());
        autoInvestPlanService.save(plan);
        return MessageResult.success("success");

    }

    /**
     * Get auto-invest plan details
     * @return
     */
    @ApiOperation(value = "Get Auto-Invest Plan Details")
    @RequestMapping("autoInvest/plan/detail")
    public MessageResult planDetail(
            Long id
    ) {
        AutoInvestPlan plan = autoInvestPlanService.getById(id);
        return success(plan);
    }


    /**
     * Update auto-invest plan status
     * @return
     */
    @ApiOperation(value = "Update Auto-Invest Plan Status")
    @RequestMapping("autoInvest/plan/updateStatus")
    public MessageResult updatePlanStatus(
            Long id,
            Integer status // status 0 enable 1 disable
    ) {
        AutoInvestPlan plan = autoInvestPlanService.getById(id);
        Assert.notNull(status, msService.getMessage("STATUS_CANNOT_BE_EMPTY"));
        plan.setStatus(status);
        plan.setUpdateTime(new Date());
        autoInvestPlanService.saveOrUpdate(plan);
        return success(plan);
    }

    /**
     * Update auto-invest plan
     * @return
     */
    @ApiOperation(value = "Update Auto-Invest Plan")
    @PermissionOperation
    @RequestMapping("autoInvest/plan/updatePlan")
    public MessageResult updatePlan(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long id,
            BigDecimal amount, // amount
            Integer cycle, // cycle 0 daily 1 weekly 2 monthly
            String startTime // start time
    ) throws ParseException {

        AutoInvestPlan plan = autoInvestPlanService.getById(id);
        AutoInvestActivity activity = autoInvestActivityService.getById(plan.getActivityId());
        if (activity.getNum().compareTo(amount) == 1) {
            return error(msService.getMessage("INVESTMENT_AMOUNT_CANNOT_BE_LESS_THAN") + activity.getNum().toPlainString());
        }
        plan.setAmount(amount);
        plan.setCycle(cycle);
        plan.setStartTime(startTime);
        // Calculate next execution time
        Date nextTime = null;
        if (cycle.intValue() == 0) { // daily
            String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -1)) + " " + startTime;
            nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
        } else if (cycle.intValue() == 1) { // weekly
            String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -8)) + " " + startTime;
            nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
        } else if (cycle.intValue() == 2) { // monthly
            String date = DateUtil.YYYY_MM_DD.format(DateUtil.getDate(new Date(), -31)) + " " + startTime;
            nextTime = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(date);
        }
        plan.setNextTime(nextTime);
        plan.setUpdateTime(new Date());
        autoInvestPlanService.saveOrUpdate(plan);
        return success(plan);
    }


    /**
     * Delete auto-invest plan
     * @return
     */
    @ApiOperation(value = "Delete Auto-Invest Plan")
    @PermissionOperation
    @RequestMapping("autoInvest/plan/delPlan")
    public MessageResult delPlan(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long id
    ) throws ParseException {
        AuthMember member = AuthMember.toAuthMember(authMember);
        AutoInvestPlan plan = autoInvestPlanService.getById(id);
        if (plan.getMemberId().longValue() == member.getId()) {
            plan.setDelFlag(1);
            plan.setUpdateTime(new Date());
            autoInvestPlanService.saveOrUpdate(plan);
        }
        return success(plan);
    }


    private BigDecimal getLastPrice(String fromUnit, String toUnit) {
        if (fromUnit.equalsIgnoreCase(toUnit)) {
            return BigDecimal.ONE;
        }
        List<CoinThumb> thumbList = marketFeign.findSymbolThumb4Feign();
        String symbol = fromUnit.toUpperCase() + "/" + toUnit.toUpperCase();
        String symbol2 = toUnit.toUpperCase() + "/" + fromUnit.toUpperCase();
        for (CoinThumb coinThumb : thumbList) {
            if (coinThumb.getSymbol().equalsIgnoreCase(symbol)) {
                return coinThumb.getClose();
            }
            if (coinThumb.getSymbol().equalsIgnoreCase(symbol2)) {
                return BigDecimal.ONE.divide(coinThumb.getClose(), 8, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ONE;
    }

}
