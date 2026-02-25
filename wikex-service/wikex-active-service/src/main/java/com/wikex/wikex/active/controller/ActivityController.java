package com.wikex.wikex.active.controller;

import com.wikex.wikex.active.entity.Activity;
import com.wikex.wikex.active.entity.ActivityOrder;
import com.wikex.wikex.active.service.ActivityOrderService;
import com.wikex.wikex.active.service.ActivityService;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.MemberLevelEnum;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.WikexRuntimeException;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static org.springframework.util.Assert.hasText;

@Api(tags = "Innovation Lab Handler")
@RestController
@RequestMapping("activity")
public class ActivityController extends BaseController {
    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityOrderService activityOrderService;

    @Autowired
    private LocaleMessageSourceService sourceService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private CoinFeign coinFeign;

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @ApiOperation(value = "Paged query")
    @RequestMapping("page-query")
    public MessageResult page(int pageNo, int pageSize, int step) {
        MessageResult mr = new MessageResult();
        Page<Activity> all = IPage2Page(activityService.queryByStep(pageNo, pageSize, step));
        mr.setCode(0);
        mr.setData(all);
        return mr;
    }

    @ApiOperation(value = "Details")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "Activity ID"),
    })
    @RequestMapping("detail")
    public MessageResult detail(Long id) {
        Activity detail = activityService.getById(id);
        Assert.notNull(detail, "validate id!");
        return success(detail);
    }

    @ApiOperation(value = "Withdrawal verification code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "amount", value = "Amount"),
            @ApiImplicitParam(name = "activityId", value = "Activity ID"),
            @ApiImplicitParam(name = "code", value = "Verification code"),
            @ApiImplicitParam(name = "aims", value = "Phone number or email"),
    })
    @PermissionOperation
    @RequestMapping("attend")
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult attendActivity(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                        BigDecimal amount,
                                        Long activityId,
                                        String code,
                                        String aims) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, sourceService.getMessage("NUMBER_OF_ILLEGAL"));
        }
        Assert.notNull(activityId, "valid activity id");

        // Check whether the verification code is correct
        hasText(code, sourceService.getMessage("MISSING_VERIFICATION_CODE"));
        hasText(aims, sourceService.getMessage("MISSING_PHONE_OR_EMAIL"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // Validate: sign-in activity, coin, member, and member wallet
        AuthMember user = AuthMember.toAuthMember(authMember);

        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) {
            return MessageResult.error(sourceService.getMessage("ACTIVITY_FAILED_PARTICIPATE"));
        }
        if (member.getMobilePhone() != null && aims.equals(member.getMobilePhone())) {
            Object info = valueOperations.get(SysConstant.PHONE_ATTEND_ACTIVITY_PREFIX + member.getMobilePhone());
            if (info == null || !info.toString().equals(code)) {
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.PHONE_ATTEND_ACTIVITY_PREFIX + member.getMobilePhone());
            }
        } else if (member.getEmail() != null && aims.equals(member.getEmail())) {
            Object info = valueOperations.get(SysConstant.PHONE_ATTEND_ACTIVITY_PREFIX + member.getEmail());
            if (!info.toString().equals(code)) {
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.PHONE_ATTEND_ACTIVITY_PREFIX + member.getEmail());
            }
        } else {
            return MessageResult.error(sourceService.getMessage("ACTIVITY_FAILED_PARTICIPATE"));
        }

        // Check if KYC (real-name) is completed
        if (member.getMemberLevel() == MemberLevelEnum.GENERAL.getCode()) {
            return MessageResult.error(500, sourceService.getMessage("NO_REALNAME"));
        }

        // Check if the member is prohibited from trading
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())) {
            return MessageResult.error(sourceService.getMessage("CANNOT_TRADE"));
        }

        // Check whether the activity exists
        Activity activity = activityService.getById(activityId);
        Assert.notNull(activity, sourceService.getMessage("ACTIVITY_NOT_FOUND"));

        // Check if the number of first-level invites meets the requirement
        if (activity.getLeveloneCount() > 0) {
            if (member.getFirstLevel() < activity.getLeveloneCount()) {
                return MessageResult.error(500, sourceService.getMessage("FIRST_LEVEL_FRIENDS_NUM_LT") + activity.getLeveloneCount());
            }
        }
        // Cloud miner check
        if (activity.getType() == 5) {
            if (amount.intValue() < 1) {
                return MessageResult.error(sourceService.getMessage("ACTIVITY_ERROR_NUMBER"));
            }
        }
        // Check whether the activity is of a valid type
        if (activity.getType() == 1 || activity.getType() == 2 || activity.getType() == 0) {
            return MessageResult.error(sourceService.getMessage("ACTIVITY_TYPE_DESCRIPTION"));
        }
        // Check whether the activity is in progress
        if (activity.getStep() != 1) {
            return MessageResult.error(sourceService.getMessage("ACTIVITY_NOT_PROGRESS"));
        }
        // Check if the current time is within the activity period
        long currentTime = Calendar.getInstance().getTimeInMillis(); // current timestamp
        try {
            if (dateTimeFormat.parse(activity.getEndTime()).getTime() < currentTime) {
                return MessageResult.error(sourceService.getMessage("ACTIVITY_ENDED"));
            }
            if (dateTimeFormat.parse(activity.getStartTime()).getTime() > currentTime) {
                return MessageResult.error(sourceService.getMessage("ACTIVITY_NOT_STARTED"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return MessageResult.error(sourceService.getMessage("UNKNOWN_ERROR") + "Code：9901");
        }

        // For free subscription types, check if exceeded the maximum issuance
        if (activity.getType() == 4 || activity.getType() == 5) {
            if (activity.getTradedAmount().compareTo(activity.getTotalSupply()) >= 0) {
                return MessageResult.error(sourceService.getMessage("EXCEEDED_SUBSCRIPTION_LIMIT"));
            }
        }
        // Minimum redemption/lock amount
        if (activity.getMinLimitAmout().compareTo(BigDecimal.ZERO) > 0) {
            if (activity.getMinLimitAmout().compareTo(amount) > 0) {
                return MessageResult.error(sourceService.getMessage("BELOW_MINIMUM_REDEMPTION_AMOUNT"));
            }
        }
        if (activity.getMaxLimitAmout().compareTo(BigDecimal.ZERO) > 0 || activity.getLimitTimes() > 0) {
            // Maximum redemption/lock amount (first, get already ordered amount)
            List<ActivityOrder> orderDetailList = activityOrderService.findAllByActivityIdAndMemberId(member.getId(), activityId);
            BigDecimal alreadyAttendAmount = BigDecimal.ZERO;
            int alreadyAttendTimes = 0;
            if (orderDetailList != null) {
                alreadyAttendTimes = orderDetailList.size();
                for (int i = 0; i < orderDetailList.size(); i++) {
                    if (activity.getType() == 3) {
                        alreadyAttendAmount = alreadyAttendAmount.add(orderDetailList.get(i).getFreezeAmount());
                    } else {
                        alreadyAttendAmount = alreadyAttendAmount.add(orderDetailList.get(i).getAmount());
                    }
                }
            }
            // Maximum per-user amount
            if (activity.getMaxLimitAmout().compareTo(BigDecimal.ZERO) > 0) {
                if (alreadyAttendAmount.add(amount).compareTo(activity.getMaxLimitAmout()) > 0) {
                    return MessageResult.error(sourceService.getMessage("EXCEEDS_MAXIMUM_REDEMPTION_AMOUNT"));
                }
            }
            // Per-user purchase limit (times)
            if (activity.getLimitTimes() > 0) {
                if (activity.getLimitTimes() < alreadyAttendTimes + 1) {
                    return MessageResult.error(sourceService.getMessage("EXCEEDS_PURCHASE_LIMIT"));
                }
            }
        }

        // Check holding requirements
        if (activity.getHoldLimit().compareTo(BigDecimal.ZERO) > 0 && activity.getHoldUnit() != null && activity.getHoldUnit() != "") {
            MemberWallet holdCoinWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getAcceptUnit(), member.getId());
            if (holdCoinWallet == null) {
                return MessageResult.error(sourceService.getMessage("REQUIRED_HOLDINGS_WALLET_MISSING"));
            }
            if (holdCoinWallet.getIsLock().equals(BooleanEnum.IS_TRUE)) {
                return MessageResult.error(sourceService.getMessage("REQUIRED_HOLDINGS_WALLET_LOCKED"));
            }
            if (holdCoinWallet.getBalance().compareTo(activity.getHoldLimit()) < 0) {
                return MessageResult.error(sourceService.getMessage("YOUR") + activity.getHoldUnit() + sourceService.getMessage("INSUFFICIENT_HOLDINGS_QUANTITY"));
            }
        }

        // Check if the coin exists
        Coin coin = coinFeign.findByUnit(activity.getAcceptUnit());
        if (coin == null) {
            return MessageResult.error(sourceService.getMessage("NONSUPPORT_COIN"));
        }

        // Check if the wallet is available
        MemberWallet acceptCoinWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getAcceptUnit(), member.getId());
        if (acceptCoinWallet == null) {
            return MessageResult.error(sourceService.getMessage("NONSUPPORT_COIN"));
        }
        if (acceptCoinWallet.getIsLock().equals(BooleanEnum.IS_TRUE)) {
            return MessageResult.error(sourceService.getMessage("WALLET_HAS_LOCKED"));
        }

        // Check whether the balance is sufficient
        BigDecimal totalAcceptCoinAmount = BigDecimal.ZERO;
        if (activity.getType() == 3) { // Holding dividend
            totalAcceptCoinAmount = amount.setScale(activity.getAmountScale(), BigDecimal.ROUND_HALF_DOWN);
        } else if (activity.getType() == 4) {  // Free subscription
            totalAcceptCoinAmount = activity.getPrice().multiply(amount).setScale(activity.getAmountScale(), BigDecimal.ROUND_HALF_DOWN);
        } else if (activity.getType() == 5) {  // Mining machine subscription
            totalAcceptCoinAmount = activity.getPrice().multiply(amount).setScale(activity.getAmountScale(), BigDecimal.ROUND_HALF_DOWN);
        } else if (activity.getType() == 6) { // Locked subscription
            // Includes locked amount and threshold fee
            totalAcceptCoinAmount = amount.add(activity.getLockedFee()).setScale(activity.getAmountScale(), BigDecimal.ROUND_HALF_DOWN);
        }

        if (acceptCoinWallet.getBalance().compareTo(totalAcceptCoinAmount) < 0) {
            return MessageResult.error(sourceService.getMessage("INSUFFICIENT_COIN") + activity.getAcceptUnit());
        }

        ActivityOrder activityOrder = new ActivityOrder();
        activityOrder.setActivityId(activityId);
        if (activity.getType() == 3) {
            activityOrder.setAmount(BigDecimal.ZERO);
            activityOrder.setFreezeAmount(totalAcceptCoinAmount); // Amount of frozen assets (only needed for holding dividend)
        } else if (activity.getType() == 4) {
            activityOrder.setAmount(amount); // Actual order quantity
            activityOrder.setFreezeAmount(BigDecimal.ZERO);
        } else if (activity.getType() == 5) {
            activityOrder.setAmount(amount); // Actual order quantity
            activityOrder.setFreezeAmount(BigDecimal.ZERO);
        } else if (activity.getType() == 6) {
            activityOrder.setAmount(amount); // Actual locked quantity
            activityOrder.setFreezeAmount(totalAcceptCoinAmount); // Frozen assets include actual locked amount and threshold fee
        } else {
            activityOrder.setAmount(BigDecimal.ZERO);
            activityOrder.setFreezeAmount(BigDecimal.ZERO);
        }
        activityOrder.setBaseSymbol(activity.getAcceptUnit());
        activityOrder.setCoinSymbol(activity.getUnit());
        activityOrder.setCreateTime(DateUtil.getCurrentDate());
        activityOrder.setMemberId(member.getId());
        activityOrder.setPrice(activity.getPrice());
        activityOrder.setState(1); // Unfilled
        activityOrder.setTurnover(totalAcceptCoinAmount); // Used as the standard for freezing or deducting assets; in lock-up activity, this is the participation quantity
        activityOrder.setType(activity.getType());

        MessageResult mr = null;
        try {
            mr = activityOrderService.saveActivityOrder(activityOrder);
            if (mr.getCode() != 0) {
                return MessageResult.error(500, sourceService.getMessage("ACTIVITY_FAILED_PARTICIPATE") + mr.getMessage());
            } else {
                return MessageResult.success(sourceService.getMessage("CONGRATULATIONS_SUBSCRIPTION_SUCCESSFUL"));
            }
        } catch (WikexRuntimeException e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("ACTIVITY_FAILED_PARTICIPATE") + mr.getMessage());
        }

    }

    @ApiOperation(value = "Get user records by activity ID")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "activityId", value = "Activity ID"),
    })
    @PermissionOperation
    @RequestMapping("getmemberrecords")
    public MessageResult getMemberRecordsByActivityId(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, Long activityId) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.notNull(activityId, "valid activity id");
        List<ActivityOrder> orderList = activityOrderService.findAllByActivityIdAndMemberId(user.getId(), activityId);

        return success(orderList);
    }

    /**
     * Get all orders the user participated in
     * @param pageNo page number
     * @param pageSize page size
     * @return MessageResult with paged orders
     */
    @ApiOperation(value = "Get all orders the user participated in")
    @RequestMapping("getmyorders")
    @PermissionOperation
    public MessageResult getMemberOrders(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.notNull(user, "valid user");
        Page<ActivityOrder> orderList = IPage2Page(activityOrderService.finaAllByMemberId(user.getId(), pageNo, pageSize));
        for (int i = 0; i < orderList.getContent().size(); i++) {
            Activity item = activityService.getById(orderList.getContent().get(i).getActivityId());
            if (item != null) {
                orderList.getContent().get(i).setActivityName(item.getTitle());
            }
            }
        return success(orderList);
    }
}
