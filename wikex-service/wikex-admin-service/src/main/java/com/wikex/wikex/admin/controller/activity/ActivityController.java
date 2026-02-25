package com.wikex.wikex.admin.controller.activity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.active.entity.Activity;
import com.wikex.wikex.active.entity.ActivityOrder;
import com.wikex.wikex.active.entity.LockedOrder;
import com.wikex.wikex.active.entity.MiningOrder;
import com.wikex.wikex.active.feign.ActivityFeign;
import com.wikex.wikex.active.feign.ActivityOrderFeign;
import com.wikex.wikex.active.feign.LockedOrderFeign;
import com.wikex.wikex.active.feign.MiningOrderFeign;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.notNull;

@RestController
@Slf4j
@RequestMapping("activity/activity")
public class ActivityController extends BaseAdminController {

	@Autowired
	private ActivityFeign activityFeign;
	@Value("${spark.system.md5.key}")
	private String md5Key;
	@Autowired
	private ActivityOrderFeign activityOrderFeign;
	@Autowired
	private LocaleMessageSourceService messageSource;
	@Autowired
	private MemberWalletFeign memberWalletFeign;
	@Autowired
	private MemberTransactionFeign memberTransactionFeign;
	@Autowired
	private MemberFeign memberFeign;

	@Autowired
	private CoinFeign coinFeign;
	@Autowired
	private MiningOrderFeign miningOrderFeign;

	@Autowired
	private LockedOrderFeign lockedOrderFeign;

	@RequiresPermissions("activity:activity:locked-activity")
	@PostMapping("locked-activity")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "View locked activity list")
	public MessageResult lockedActivityList() {
		// Query locked activities that are currently in progress
		List<Activity> all = activityFeign.lockedActivityList();
		return success(all);
	}

	/**
	 * Paginated query
	 */
	@RequiresPermissions("activity:activity:page-query")
	@PostMapping("page-query")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "Paginated view of activity list")
	public MessageResult activityList(PageParam pageParam) {
		Page<Activity> all = activityFeign.findAll(pageParam);
		return success(IPage2Page(all));
	}

	/**
	 * Add activity information
	 */
	@RequiresPermissions("activity:activity:add")
	@PostMapping("add")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "Create activity")
	public MessageResult ExchangeCoinList(@Valid Activity activity) {
		activity.setCreateTime(DateUtil.getCurrentDate());
		activity = activityFeign.save(activity);
		return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), activity);
	}

	@RequiresPermissions("activity:activity:orderlist")
	@GetMapping("{aid}/orderlist")
	public MessageResult orderList(@PathVariable Long aid) {
		List<ActivityOrder> activityOrderList = activityOrderFeign.findAllByActivityId(aid);
		Assert.notNull(activityOrderList, "validate id!");
		return success(activityOrderList);
	}

	/**
	 * Modify activity progress value
	 */
	@RequiresPermissions("activity:activity:modify-progress")
	@PostMapping("modify-progress")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "Update activity progress")
	public MessageResult alterActivity(
			@RequestParam("id") Long id,
			@RequestParam("progress") Integer progress) {
		notNull(id, "validate id!");

		Activity result = activityFeign.findById(id);
		notNull(result, "validate activity!");

		if (result.getProgress() > progress.intValue()) {
			return error(messageSource.getMessage("NEW_PROGRESS_LESS_THAN_CURRENT"));
		}
		result.setProgress(progress);

		activityFeign.save(result);

		return success(messageSource.getMessage("SUCCESS"));
	}

	// Modify
	@RequiresPermissions("activity:activity:modify")
	@PostMapping("modify")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "Update activity info")
	public MessageResult alterActivity(
			@RequestParam("id") Long id,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "detail", required = false) String detail,
			@RequestParam(value = "status", required = false) Integer status,
			@RequestParam(value = "step", required = false) Integer step,
			@RequestParam(value = "type", required = false) Integer type,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "totalSupply", required = false) BigDecimal totalSupply,
			@RequestParam(value = "price", required = false) BigDecimal price,
			@RequestParam(value = "priceScale", required = false) Integer priceScale,
			@RequestParam(value = "unit", required = false) String unit,
			@RequestParam(value = "acceptUnit", required = false) String acceptUnit,
			@RequestParam(value = "amountScale", required = false) Integer amountScale,
			@RequestParam(value = "maxLimitAmout", required = false) BigDecimal maxLimitAmout,
			@RequestParam(value = "minLimitAmout", required = false) BigDecimal minLimitAmout,
			@RequestParam(value = "limitTimes", required = false) Integer limitTimes,
			@RequestParam(value = "settings", required = false) String settings,
			@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "smallImageUrl", required = false) String smallImageUrl,
			@RequestParam(value = "bannerImageUrl", required = false) String bannerImageUrl,
			@RequestParam(value = "noticeLink", required = false) String noticeLink,
			@RequestParam(value = "activityLink", required = false) String activityLink,
			@RequestParam(value = "leveloneCount", required = false) Integer leveloneCount,
			@RequestParam(value = "holdLimit", required = false) BigDecimal holdLimit,
			@RequestParam(value = "holdUnit", required = false) String holdUnit,
			@RequestParam(value = "miningDays", required = false) Integer miningDays,
			@RequestParam(value = "miningDaysprofit", required = false) BigDecimal miningDaysprofit,
			@RequestParam(value = "miningUnit", required = false) String miningUnit,
			@RequestParam(value = "miningInvite", required = false) BigDecimal miningInvite,
			@RequestParam(value = "miningInvitelimit", required = false) BigDecimal miningInvitelimit,
			@RequestParam(value = "miningPeriod", required = false) Integer miningPeriod,
			@RequestParam(value = "lockedUnit", required = false) String lockedUnit,
			@RequestParam(value = "lockedPeriod", required = false) Integer lockedPeriod,
			@RequestParam(value = "lockedDays", required = false) Integer lockedDays,
			@RequestParam(value = "releaseType", required = false) Integer releaseType,
			@RequestParam(value = "releasePercent", required = false) BigDecimal releasePercent,
			@RequestParam(value = "lockedFee", required = false) BigDecimal lockedFee,
			@RequestParam(value = "releaseAmount", required = false) BigDecimal releaseAmount,
			@RequestParam(value = "releaseTimes", required = false) BigDecimal releaseTimes,

			@RequestParam(value = "password") String password,
			@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) throws Exception {
		password = MD5.md5(password + md5Key);
		Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));

		Activity result = activityFeign.findById(id);

		notNull(result, "validate activity!");

		if (title != null)
			result.setTitle(title);
		if (detail != null)
			result.setDetail(detail);
		if (status != null)
			result.setStatus(status);
		if (step != null)
			result.setStep(step);
		if (type != null)
			result.setType(type);
		if (startTime != null)
			result.setStartTime(startTime);
		if (endTime != null)
			result.setEndTime(endTime);
		if (totalSupply != null)
			result.setTotalSupply(totalSupply);
		if (price != null)
			result.setPrice(price);
		if (priceScale != null)
			result.setPriceScale(priceScale);
		if (unit != null)
			result.setUnit(unit);
		if (acceptUnit != null)
			result.setAcceptUnit(acceptUnit);
		if (amountScale != null)
			result.setAmountScale(amountScale);
		if (maxLimitAmout != null)
			result.setMaxLimitAmout(maxLimitAmout);
		if (minLimitAmout != null)
			result.setMinLimitAmout(minLimitAmout);
		if (limitTimes != null)
			result.setLimitTimes(limitTimes);
		if (settings != null)
			result.setSettings(settings);
		if (content != null)
			result.setContent(content);
		if (smallImageUrl != null)
			result.setSmallImageUrl(smallImageUrl);
		if (bannerImageUrl != null)
			result.setBannerImageUrl(bannerImageUrl);
		if (noticeLink != null)
			result.setNoticeLink(noticeLink);
		if (activityLink != null)
			result.setActivityLink(activityLink);
		if (leveloneCount != null)
			result.setLeveloneCount(leveloneCount);
		if (holdLimit != null)
			result.setHoldLimit(holdLimit);
		if (holdUnit != null)
			result.setHoldUnit(holdUnit);
		if (miningDays != null)
			result.setMiningDays(miningDays);
		if (miningDaysprofit != null)
			result.setMiningDaysprofit(miningDaysprofit);
		if (miningUnit != null)
			result.setMiningUnit(miningUnit);
		if (miningInvite != null)
			result.setMiningInvite(miningInvite);
		if (miningInvitelimit != null)
			result.setMiningInvitelimit(miningInvitelimit);
		if (miningPeriod != null)
			result.setMiningPeriod(miningPeriod);
		if (lockedUnit != null)
			result.setLockedUnit(lockedUnit);
		if (lockedPeriod != null)
			result.setLockedPeriod(lockedPeriod);
		if (lockedDays != null)
			result.setLockedDays(lockedDays);
		if (releaseType != null)
			result.setReleaseType(releaseType);
		if (releasePercent != null)
			result.setReleasePercent(releasePercent);
		if (lockedFee != null)
			result.setLockedFee(lockedFee);
		if (releaseAmount != null)
			result.setReleaseAmount(releaseAmount);
		if (releaseTimes != null)
			result.setReleaseTimes(releaseTimes);

		activityFeign.save(result);
		return success(messageSource.getMessage("SUCCESS"));
	}

	@RequiresPermissions("activity:activity:detail")
	@GetMapping("{id}/detail")
	public MessageResult detail(@PathVariable Long id) {
		Activity activity = activityFeign.findById(id);
		Assert.notNull(activity, "validate id!");
		return success(activity);
	}

	/**
	 * Distribute activity coins
	 */
	@RequiresPermissions("activity:activity:distribute")
	@PostMapping("distribute")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "Distribute activity coins")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult distribute(@RequestParam("oid") Long oid) {
		ActivityOrder order = activityOrderFeign.findById(oid);
		
		if (order == null) {
			return error(messageSource.getMessage("ORDER_NOT_FOUND"));
		}
		if (order.getState() != 1) {
			return error(messageSource.getMessage("ACTIVITY_NOT_FOUND"));
		}
		Activity activity = activityFeign.findById(order.getActivityId());
		
		if (activity == null) {
			return error(messageSource.getMessage("ACTIVITY_NOT_YET_ENDED"));
		}
		// Types 1, 2, 3, 4 need to be in the distribution phase
		if (activity.getType() == 1 || activity.getType() == 2 || activity.getType() == 3 || activity.getType() == 4) {
			// Activity must be finished and in distribution phase
			if (activity.getStep() != 2) {
				return error("This activity is not in the distribution phase");
			}
		}

		// type = 3 (holding distribution)
		// Users split the pool by holding ratio without spending any coins
		if (activity.getType() == 3) {
			// Unfreeze accepted coin (acceptUnit)
			MemberWallet freezeWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getAcceptUnit(),
					order.getMemberId());
			
			if (freezeWallet == null) {
				return error(messageSource.getMessage("ACTIVITY_COIN_WALLET_NOT_FOUND"));
			}
			memberWalletFeign.thawBalance(freezeWallet.getCoinId(), order.getMemberId(), order.getFreezeAmount());

			// Distribute activity coin (unit) — get wallet of activity coin
			MemberWallet distributeWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getUnit(),
					order.getMemberId());

			if (distributeWallet == null) {
				return error(messageSource.getMessage("HOLDINGS_ACTIVITY_DISTRIBUTION"));
			}
			// Distribution amount = holding / total holding * activity total supply
			BigDecimal disAmount = order.getFreezeAmount()
					.divide(activity.getFreezeAmount())
					.multiply(activity.getTotalSupply())
					.setScale(activity.getAmountScale(), BigDecimal.ROUND_HALF_DOWN);
			// Increase user wallet balance
			memberWalletFeign.increaseBalance(distributeWallet.getId(), disAmount);

			MemberTransaction memberTransaction = new MemberTransaction();
			memberTransaction.setFee(BigDecimal.ZERO);
			memberTransaction.setAmount(disAmount);
			memberTransaction.setMemberId(distributeWallet.getMemberId());
			memberTransaction.setSymbol(activity.getUnit());
			memberTransaction.setType(TransactionType.ACTIVITY_BUY.getCode());
			memberTransaction.setCreateTime(DateUtil.getCurrentDate());
			memberTransaction.setRealFee("0");
			memberTransaction.setDiscountFee("0");
			memberTransactionFeign.save(memberTransaction);

			// Update order status
			order.setState(2); // completed
			order.setAmount(disAmount); // filled amount
			activityOrderFeign.save(order);

			return success(messageSource.getMessage("HOLDINGS_ACTIVITY_DISTRIBUTION") + ":" + disAmount);
		}

		// type = 4 (free subscription)
		if (activity.getType() == 4) {
			// Deduct accepted coin (finalize)
			MemberWallet freezeWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getAcceptUnit(),
					order.getMemberId());
			
			if (freezeWallet == null) {
				return error(messageSource.getMessage("FROZEN_COIN_WALLET_NOT_FOUND"));
			}
			memberWalletFeign.decreaseFrozen(freezeWallet.getId(), order.getTurnover());

			MemberTransaction memberTransaction1 = new MemberTransaction();
			memberTransaction1.setFee(BigDecimal.ZERO);
			memberTransaction1.setAmount(order.getTurnover().negate());
			memberTransaction1.setMemberId(freezeWallet.getMemberId());
			memberTransaction1.setSymbol(activity.getAcceptUnit());
			memberTransaction1.setType(TransactionType.ACTIVITY_BUY.getCode());
			memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
			memberTransaction1.setRealFee("0");
			memberTransaction1.setDiscountFee("0");
			memberTransactionFeign.save(memberTransaction1);

			// Distribute activity coin
			BigDecimal disAmount = order.getAmount();
			MemberWallet distributeWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getUnit(),
					order.getMemberId());
			if (distributeWallet == null) {
				return error(messageSource.getMessage("ACTIVITY_COIN_WALLET_NOT_FOUND"));
			}
			memberWalletFeign.increaseBalance(distributeWallet.getId(), disAmount);

			MemberTransaction memberTransaction = new MemberTransaction();
			memberTransaction.setFee(BigDecimal.ZERO);
			memberTransaction.setAmount(disAmount);
			memberTransaction.setMemberId(distributeWallet.getMemberId());
			memberTransaction.setSymbol(activity.getUnit());
			memberTransaction.setType(TransactionType.ACTIVITY_BUY.getCode());
			memberTransaction.setCreateTime(DateUtil.getCurrentDate());
			memberTransaction.setRealFee("0");
			memberTransaction.setDiscountFee("0");
			memberTransactionFeign.save(memberTransaction);

			// Update order status
			order.setState(2); // completed
			activityOrderFeign.save(order);

			return success("Free subscription distribution completed, distributed amount: " + disAmount);
		}

		// Cloud miner sale type
		if (activity.getType() == 5) {
			// Deduct accepted coin (finalize)
			MemberWallet freezeWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getAcceptUnit(),
					order.getMemberId());
			
			if (freezeWallet == null) {
				return error(messageSource.getMessage("FROZEN_COIN_WALLET_NOT_FOUND"));
			}
			memberWalletFeign.decreaseFrozen(freezeWallet.getId(), order.getTurnover());

			MemberTransaction memberTransaction1 = new MemberTransaction();
			memberTransaction1.setFee(BigDecimal.ZERO);
			memberTransaction1.setAmount(order.getTurnover().negate());
			memberTransaction1.setMemberId(freezeWallet.getMemberId());
			memberTransaction1.setSymbol(activity.getAcceptUnit());
			memberTransaction1.setType(TransactionType.ACTIVITY_BUY.getCode());
			memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
			memberTransaction1.setRealFee("0");
			memberTransaction1.setDiscountFee("0");
			memberTransactionFeign.save(memberTransaction1);

			// Update order status
			order.setState(2); // completed
			activityOrderFeign.save(order);

			// Generate mining machines
			for (int i = 0; i < order.getAmount().intValue(); i++) {
				Date currentDate = DateUtil.getCurrentDate();
				MiningOrder mo = new MiningOrder();
				mo.setActivityId(activity.getId());
				mo.setMemberId(order.getMemberId());
				mo.setMiningDays(activity.getMiningDays());
				mo.setMiningDaysprofit(activity.getMiningDaysprofit());
				mo.setMiningUnit(activity.getMiningUnit());
				mo.setCurrentDaysprofit(activity.getMiningDaysprofit());
				mo.setCreateTime(currentDate);
				mo.setEndTime(DateUtil.dateAddDay(currentDate, activity.getMiningDays()));
				mo.setImage(activity.getSmallImageUrl());
				mo.setTitle(activity.getTitle());
				mo.setMiningStatus(1); // mining status (1: mining)
				mo.setMiningedDays(0); // initially 0 days
				mo.setTotalProfit(BigDecimal.ZERO);
				mo.setType(0); // general miner
				mo.setMiningInvite(activity.getMiningInvite()); // invite bonus
				mo.setMiningInvitelimit(activity.getMiningInvitelimit()); // max production increase
				mo.setPeriod(activity.getMiningPeriod()); // mining output period
				miningOrderFeign.save(mo);
			}
			Member member = memberFeign.findMemberById(order.getMemberId());
			// Whether inviting can increase production capacity (each invite boosts only
			// one miner)
			if (activity.getMiningInvite().compareTo(BigDecimal.ZERO) > 0) {
				if (member != null) {
					if (member.getInviterId() != null) {
						Member inviter = memberFeign.findMemberById(member.getInviterId());
						List<MiningOrder> miningOrders = miningOrderFeign
								.findAllByMemberIdAndActivityId(inviter.getId(), activity.getId());
						if (miningOrders.size() > 0) {
							for (MiningOrder item : miningOrders) {
								// If current capacity is below the limit
								if (item.getCurrentDaysprofit()
										.subtract(item.getMiningDaysprofit())
										.divide(item.getMiningDaysprofit())
										.compareTo(activity.getMiningInvitelimit()) < 0) {
									// New capacity
									BigDecimal newMiningDaysprofit = item.getCurrentDaysprofit()
											.add(item.getMiningDaysprofit().multiply(activity.getMiningInvite()));
									// Cap at limit
									if (newMiningDaysprofit.compareTo(
											item.getMiningDaysprofit().add(item.getMiningDaysprofit()
													.multiply(activity.getMiningInvitelimit()))) > 0) {
										newMiningDaysprofit = item.getMiningDaysprofit()
												.add(item.getMiningDaysprofit()
														.multiply(activity.getMiningInvitelimit()));
									}
									item.setCurrentDaysprofit(newMiningDaysprofit);
									miningOrderFeign.save(item);
									break;
								}
							}
						}
					}
				}
			}

			return success(messageSource.getMessage("MINING_MACHINE_DEPLOYED"));
		}

		// Locking/vesting type
		if (activity.getType() == 6) {
			if (activity.getReleaseTimes().compareTo(BigDecimal.ZERO) <= 0 || activity.getReleaseTimes() == null) {
				return error(messageSource.getMessage("RELEASE_MULTIPLIER_CANNOT_BE_ZERO"));
			}
			// Deduct accepted coin (finalize) — includes locked amount + threshold fee
			MemberWallet freezeWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getAcceptUnit(),
					order.getMemberId());
			if (freezeWallet == null) {
				return error(messageSource.getMessage("FROZEN_COIN_WALLET_NOT_FOUND"));
			}
			memberWalletFeign.decreaseFrozen(freezeWallet.getId(), order.getTurnover());

			MemberTransaction memberTransaction1 = new MemberTransaction();
			memberTransaction1.setFee(BigDecimal.ZERO);
			memberTransaction1.setAmount(order.getTurnover().negate());
			memberTransaction1.setMemberId(freezeWallet.getMemberId());
			memberTransaction1.setSymbol(activity.getAcceptUnit());
			memberTransaction1.setType(TransactionType.ACTIVITY_BUY.getCode());
			memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
			memberTransaction1.setRealFee("0");
			memberTransaction1.setDiscountFee("0");
			memberTransactionFeign.save(memberTransaction1);

			// ToRelease: increase pending release balance (= user participation amount *
			// release multiplier)
			memberWalletFeign.increaseToRelease(freezeWallet.getId(),
					order.getAmount().multiply(activity.getReleaseTimes()));

			// Update order status
			order.setState(2); // completed
			activityOrderFeign.save(order);

			// Generate locking order
			Date currentDate = DateUtil.getCurrentDate();
			LockedOrder lo = new LockedOrder();
			lo.setActivityId(activity.getId());
			lo.setMemberId(order.getMemberId());
			lo.setLockedDays(activity.getLockedDays());
			lo.setReleasedDays(0);
			lo.setReleaseUnit(activity.getLockedUnit());
			lo.setReleaseType(activity.getReleaseType());
			lo.setPeriod(activity.getLockedPeriod());
			lo.setLockedStatus(1); // locking status: releasing
			lo.setReleasePercent(activity.getReleasePercent());
			lo.setReleaseCurrentpercent(activity.getReleasePercent());
			lo.setImage(activity.getSmallImageUrl());
			lo.setTitle(activity.getTitle());
			lo.setTotalLocked(order.getAmount().multiply(activity.getReleaseTimes()));
			lo.setReleaseTimes(activity.getReleaseTimes());
			lo.setOriginReleaseamount(order.getAmount().multiply(activity.getReleaseTimes())
					.divide(BigDecimal.valueOf(activity.getLockedDays()), 8, BigDecimal.ROUND_HALF_DOWN));
			lo.setCurrentReleaseamount(order.getAmount().multiply(activity.getReleaseTimes())
					.divide(BigDecimal.valueOf(activity.getLockedDays()), 8, BigDecimal.ROUND_HALF_DOWN));
			lo.setTotalRelease(BigDecimal.ZERO);
			lo.setLockedInvite(activity.getMiningInvite());
			lo.setLockedInvitelimit(activity.getMiningInvitelimit());
			lo.setCreateTime(new Date());
			// Release by day
			if (activity.getLockedPeriod() == 0)
				lo.setEndTime(DateUtil.dateAddDay(currentDate, activity.getLockedDays()));
			// Release by week
			if (activity.getLockedPeriod() == 1)
				lo.setEndTime(DateUtil.dateAddDay(currentDate, activity.getLockedDays() * 7));
			// Release by month
			if (activity.getLockedPeriod() == 2)
				lo.setEndTime(DateUtil.dateAddMonth(currentDate, activity.getLockedDays()));
			// Release by year
			if (activity.getLockedPeriod() == 3)
				lo.setEndTime(DateUtil.dateAddYear(currentDate, activity.getLockedDays()));

			lockedOrderFeign.save(lo);

			Member member = memberFeign.findMemberById(order.getMemberId());
			// Whether inviting can increase release capacity (each invite boosts only one
			// lock order)
			if (activity.getMiningInvite().compareTo(BigDecimal.ZERO) > 0) {
				if (member != null) {
					if (member.getInviterId() != null) {
						Member inviter = memberFeign.findMemberById(member.getInviterId());
						List<LockedOrder> lockedOrders = lockedOrderFeign
								.findAllByMemberIdAndActivityId(inviter.getId(), activity.getId());
						if (lockedOrders.size() > 0) {
							for (LockedOrder item : lockedOrders) {
								// If current release amount is below the maximum increase
								if (item.getCurrentReleaseamount()
										.subtract(item.getOriginReleaseamount())
										.divide(item.getOriginReleaseamount())
										.compareTo(activity.getMiningInvitelimit()) < 0) {
									// New release amount
									BigDecimal newReleaseAmount = item.getCurrentReleaseamount()
											.add(item.getCurrentReleaseamount().multiply(activity.getMiningInvite()));
									// Cap at limit
									if (newReleaseAmount.compareTo(
											item.getOriginReleaseamount().add(item.getOriginReleaseamount()
													.multiply(activity.getMiningInvitelimit()))) > 0) {
										newReleaseAmount = item.getOriginReleaseamount()
												.add(item.getOriginReleaseamount()
														.multiply(activity.getMiningInvitelimit()));
									}
									item.setCurrentReleaseamount(newReleaseAmount);
									lockedOrderFeign.save(item);
									break;
								}
							}
						}
					}
				}
			}

			return success(messageSource.getMessage("APPROVAL_SUCCESS"));
		}

		if (activity.getType() == 6) {
			// no-op (kept intentionally)
		}
		return error(messageSource.getMessage("UNKNOWN_ACTIVITY_TYPE"));
	}

	/**
	 * Manually lock user assets by admin
	 */
	@RequiresPermissions("activity:activity:lock-member-coin")
	@PostMapping("lock-member-coin")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "Admin manual lock user assets")
	public MessageResult lockMemberCoin(@RequestParam("memberId") Long memberId,
			@RequestParam("activityId") Long activityId,
			@RequestParam("unit") String unit,
			@RequestParam("amount") BigDecimal amount) {
		// Check user existence
		Member member = memberFeign.findMemberById(memberId);
		org.springframework.util.Assert.notNull(member, messageSource.getMessage("USER"));

		// Check activity existence
		Activity activity = activityFeign.findById(activityId);
		org.springframework.util.Assert.notNull(activity, messageSource.getMessage("ACTIVITY_NOT_FOUND"));
		// Must be locking activity
		if (activity.getType() != 6) {
			return MessageResult.error(messageSource.getMessage("NOT_LOCKING_ACTIVITY"));
		}
		// Activity must be in progress
		if (activity.getStep() != 1) {
			return MessageResult.error(messageSource.getMessage("ACTIVITY_NOT_IN_PROGRESS"));
		}

		// Minimum exchange/lock amount
		if (activity.getMinLimitAmout().compareTo(BigDecimal.ZERO) > 0) {
			if (activity.getMinLimitAmout().compareTo(amount) > 0) {
				return MessageResult.error(messageSource.getMessage("BELOW_MINIMUM_REDEMPTION_AMOUNT"));
			}
		}
		if (activity.getMaxLimitAmout().compareTo(BigDecimal.ZERO) > 0 || activity.getLimitTimes() > 0) {
			// Max exchange/lock amount (first get already placed amount)
			List<ActivityOrder> orderDetailList = activityOrderFeign.findAllByActivityIdAndMemberId(activityId,
					member.getId());
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
			// Max purchase amount
			if (activity.getMaxLimitAmout().compareTo(BigDecimal.ZERO) > 0) {
				if (alreadyAttendAmount.add(amount).compareTo(activity.getMaxLimitAmout()) > 0) {
					return MessageResult.error(messageSource.getMessage("EXCEEDS_MAXIMUM_REDEMPTION_AMOUNT"));
				}
			}
			// Personal purchase times limit
			if (activity.getLimitTimes() > 0) {
				if (activity.getLimitTimes() < alreadyAttendTimes + 1) {
					return MessageResult.error(messageSource.getMessage("EXCEEDS_PURCHASE_LIMIT"));
				}
			}
		}

		// Holding requirement
		if (activity.getHoldLimit().compareTo(BigDecimal.ZERO) > 0 && activity.getHoldUnit() != null
				&& activity.getHoldUnit() != "") {
			MemberWallet holdCoinWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getHoldUnit(),
					member.getId());
			if (holdCoinWallet == null) {
				return MessageResult.error(messageSource.getMessage("REQUIRED_HOLDINGS_WALLET_MISSING"));
			}
			if (holdCoinWallet.getIsLock().equals(BooleanEnum.IS_TRUE)) {
				return MessageResult.error(messageSource.getMessage("REQUIRED_HOLDINGS_WALLET_LOCKED"));
			}
			if (holdCoinWallet.getBalance().compareTo(activity.getHoldLimit()) < 0) {
				return MessageResult.error(messageSource.getMessage("YOUR") + activity.getHoldUnit()
						+ messageSource.getMessage("INSUFFICIENT_HOLDINGS_QUANTITY"));
			}
		}

		// Check coin existence
		Coin coin;
		coin = coinFeign.findByUnit(activity.getAcceptUnit());
		if (coin == null) {
			return MessageResult.error(messageSource.getMessage("CURRENCY_NOT_FOUND"));
		}

		// Check wallet availability
		MemberWallet acceptCoinWallet = memberWalletFeign.findByCoinUnitAndMemberId(activity.getAcceptUnit(),
				member.getId());
		if (acceptCoinWallet == null || acceptCoinWallet == null) {
			return MessageResult.error(messageSource.getMessage("USER_WALLET_NOT_FOUND"));
		}
		if (acceptCoinWallet.getIsLock().equals(BooleanEnum.IS_TRUE)) {
			return MessageResult.error(messageSource.getMessage("USER_WALLET_LOCKED"));
		}

		// Check balance sufficiency
		BigDecimal totalAcceptCoinAmount = BigDecimal.ZERO;
		// Includes locked amount + threshold fee
		totalAcceptCoinAmount = amount.add(activity.getLockedFee()).setScale(activity.getAmountScale(),
				BigDecimal.ROUND_HALF_DOWN);

		if (acceptCoinWallet.getBalance().compareTo(totalAcceptCoinAmount) < 0) {
			return MessageResult.error(messageSource.getMessage("INSUFFICIENT_USER_BALANCE"));
		}

		ActivityOrder activityOrder = new ActivityOrder();
		activityOrder.setActivityId(activityId);
		activityOrder.setAmount(amount); // actual locked amount
		activityOrder.setFreezeAmount(totalAcceptCoinAmount); // includes user locked amount + threshold fee
		activityOrder.setBaseSymbol(activity.getAcceptUnit());
		activityOrder.setCoinSymbol(activity.getUnit());
		activityOrder.setCreateTime(DateUtil.getCurrentDate());
		activityOrder.setMemberId(member.getId());
		activityOrder.setPrice(activity.getPrice());
		activityOrder.setState(1); // not filled
		// Used as the standard for freezing or deducting assets; for locking
		// activities, this is the participation amount
		activityOrder.setTurnover(totalAcceptCoinAmount);
		activityOrder.setType(activity.getType());

		MessageResult mr = activityOrderFeign.saveActivityOrder(activityOrder);

		if (mr.getCode() != 0) {
			return MessageResult.error(500,
					messageSource.getMessage("ACTIVITY_PARTICIPATION_FAILED") + ":" + mr.getMessage());
		} else {
			return MessageResult.success(messageSource.getMessage("LOCK_SUBMITTED_SUCCESS"));
		}
	}
}
