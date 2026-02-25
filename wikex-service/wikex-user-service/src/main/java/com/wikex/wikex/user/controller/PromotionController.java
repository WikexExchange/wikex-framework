package com.wikex.wikex.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.rewardHub.RewardHubHandler;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.MemberPromotionHistoryVO;
import com.wikex.wikex.user.vo.RewardRecordCommisionVO;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.GeneratorUtil;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Api(tags = "Promotion")
@RestController
@Slf4j
@RequestMapping(value = "/promotion")
public class PromotionController extends BaseController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private RewardRecordService rewardRecordService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private MemberInviteStasticService memberInviteStasticService;

    @Autowired
    private PromotionCardService promotionCardService;

    @Autowired
    private PromotionCardOrderService promotionCardOrderService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberPromotionService memberPromotionService;

    @Autowired
    private LocaleMessageSourceService sourceService;

    @Autowired
    private RewardHubHandler rewardHandler;

    private Random rand = new Random();

    /**
     * Get promotion partner information
     */
    @ApiOperation(value = "Get promotion partner information")
    @PermissionOperation
    @RequestMapping(value = "/mypromotion")
    public MessageResult myPromotioin(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Long memberId = user.getId();
        MemberInviteStastic result = memberInviteStasticService.findByMemberId(memberId);
        JSONObject data = new JSONObject();

        // totalInvite
        Long totalF1 = memberPromotionService.countByInviterId(memberId);
        data.put("totalF1", totalF1 != null ? totalF1 : 0L);

        List<Long> friendIds = memberService.findPromotionMemberIds(memberId);
        if (friendIds.size() == 0) {
            data.put("totalTradingVolume", BigDecimal.ZERO);
            data.put("totalTradingFee", BigDecimal.ZERO);
        } else {
            // totalTradingVolume
            BigDecimal totalTradingVolumeF1 = rewardHandler.sumVolumnByMemberIds(friendIds,
                    CampaignBonusType.TRADING_VOLUMN.getCode());
            data.put("totalTradingVolume", totalTradingVolumeF1);

            // totalTradingFee
            BigDecimal totalTradingFee = rewardHandler.sumVolumnByMemberIds(friendIds,
                    CampaignBonusType.COMMISION_FEE.getCode());
            data.put("totalTradingFee", totalTradingFee);
        }

        // totalCommission
        List<RewardRecordType> types = new ArrayList<>();
        types.add(RewardRecordType.PROMOTION);
        types.add(RewardRecordType.CAMPAIGN_TRADING_VOLUMN_CLAIM);
        types.add(RewardRecordType.CAMPAIGN_TRADING_FRIEND_CLAIM);
        types.add(RewardRecordType.CAMPAIGN_INVITER_CLAIM);
        types.add(RewardRecordType.CAMPAIGN_EXCHANGE_FEE);
        BigDecimal totalCommission = rewardRecordService.getTotalCommissionByMemberId(memberId, types);
        data.put("totalCommission", totalCommission != null ? totalCommission : BigDecimal.ZERO);

        return success(data);
    }

    /**
     * Referal History
     */
    @ApiOperation(value = "Referal History")
    @PermissionOperation
    @RequestMapping(value = "/referalHistory")
    public MessageResult referalHistory(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();
        IPage<MemberPromotion> pageList = memberPromotionService.findMemberPromotionPage(pageNo, pageSize,
                memberId);
        List<MemberPromotion> list = pageList.getRecords();
        List<MemberPromotionHistoryVO> results = new ArrayList<>();
        if (list.size() > 0) {
            List<Long> invitees_ids = list.stream().map(MemberPromotion::getInviteesId).collect(Collectors.toList());
            Map<Long, Member> invitees_dict = memberService.mapByMemberIds(invitees_ids);
            for (MemberPromotion v : list) {
                if (invitees_dict.containsKey(v.getInviteesId())) {
                    Member mem = invitees_dict.get(v.getInviteesId());
                    MemberPromotionHistoryVO rel = new MemberPromotionHistoryVO();
                    rel.setName(mem.getEmail());
                    rel.setUid(mem.getId());
                    rel.setIsKyc(false);
                    rel.setLevel(v.getLevel() + 1);
                    rel.setCreateTime(v.getCreateTime());
                    results.add(rel);
                }
            }
        }
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(results);
        messageResult.setTotalPage(pageList.getPages() + "");
        messageResult.setTotalElement(pageList.getTotal() + "");
        return messageResult;
    }

    /**
     * Weekly promotion ranking
     */
    @ApiOperation(value = "Weekly promotion ranking")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "top", value = "top"),
    })
    @RequestMapping(value = "/weektoprank")
    public MessageResult topRankWeek(@RequestParam(value = "top", defaultValue = "20") Integer top) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        JSONObject result = (JSONObject) valueOperations.get(SysConstant.MEMBER_PROMOTION_TOP_RANK_WEEK + top);
        if (result != null) {
            return success(result);
        } else {
            JSONObject resultObj = new JSONObject();
            // Weekly ranking
            List<MemberInviteStasticRank> topInviteWeek = memberInviteStasticService.topInviteCountByType(1, 20);
            for (MemberInviteStasticRank item3 : topInviteWeek) {
                item3.setUserIdentify(item3.getUserIdentify().substring(0, 3) + "****"
                        + item3.getUserIdentify().substring(item3.getUserIdentify().length() - 4));
            }
            resultObj.put("topinviteweek", topInviteWeek);
            valueOperations.set(SysConstant.MEMBER_PROMOTION_TOP_RANK_WEEK + top, resultObj,
                    SysConstant.MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME_WEEK, TimeUnit.SECONDS);
            return success(resultObj);
        }
    }

    /**
     * Monthly promotion ranking
     */
    @ApiOperation(value = "Monthly promotion ranking")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "top", value = "top"),
    })
    @RequestMapping(value = "/monthtoprank")
    public MessageResult topRankMonth(@RequestParam(value = "top", defaultValue = "20") Integer top) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        JSONObject result = (JSONObject) valueOperations.get(SysConstant.MEMBER_PROMOTION_TOP_RANK_MONTH + top);
        if (result != null) {
            return success(result);
        } else {
            JSONObject resultObj = new JSONObject();
            // Monthly ranking
            List<MemberInviteStasticRank> topInviteMonth = memberInviteStasticService.topInviteCountByType(2, 20);
            for (MemberInviteStasticRank item4 : topInviteMonth) {
                item4.setUserIdentify(item4.getUserIdentify().substring(0, 3) + "****"
                        + item4.getUserIdentify().substring(item4.getUserIdentify().length() - 4));
            }
            resultObj.put("topinvitemonth", topInviteMonth);
            valueOperations.set(SysConstant.MEMBER_PROMOTION_TOP_RANK_MONTH + top, resultObj,
                    SysConstant.MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME_MONTH, TimeUnit.SECONDS);
            return success(resultObj);
        }
    }

    /**
     * Get rebate amount & invite count of top N
     */
    @ApiOperation(value = "Get rebate amount & invite count of top N")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "top", value = "top"),
    })
    @RequestMapping(value = "/toprank")
    public MessageResult topRank(@RequestParam(value = "top", defaultValue = "20") Integer top) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        JSONObject result = (JSONObject) valueOperations.get(SysConstant.MEMBER_PROMOTION_TOP_RANK + top);
        if (result != null) {
            return success(result);
        } else {
            JSONObject resultObj = new JSONObject();
            List<MemberInviteStastic> topReward = memberInviteStasticService.topRewardAmount(top);

            for (MemberInviteStastic item1 : topReward) {
                if (!StringUtils.isEmpty(item1.getUserIdentify())) {
                    item1.setUserIdentify(item1.getUserIdentify().substring(0, 3) + "****"
                            + item1.getUserIdentify().substring(item1.getUserIdentify().length() - 4));
                }
                item1.setMemberId(item1.getMemberId() * (item1.getMemberId() % 100)); // Just to hide real ID
            }

            List<MemberInviteStastic> topInvite = memberInviteStasticService.topInviteCount(top);
            for (MemberInviteStastic item2 : topInvite) {
                if (!StringUtils.isEmpty(item2.getUserIdentify())) {
                    item2.setUserIdentify(item2.getUserIdentify().substring(0, 3) + "****"
                            + item2.getUserIdentify().substring(item2.getUserIdentify().length() - 4));
                }
                item2.setMemberId(item2.getMemberId() * (item2.getMemberId() % 100));
            }
            resultObj.put("topreward", topReward);
            resultObj.put("topinvite", topInvite);

            // Weekly ranking
            List<MemberInviteStasticRank> topInviteWeek = memberInviteStasticService.topInviteCountByType(1, 20);
            for (MemberInviteStasticRank item3 : topInviteWeek) {
                if (!StringUtils.isEmpty(item3.getUserIdentify())) {
                    item3.setUserIdentify(item3.getUserIdentify().substring(0, 3) + "****"
                            + item3.getUserIdentify().substring(item3.getUserIdentify().length() - 4));
                }
                item3.setMemberId(item3.getMemberId() * (item3.getMemberId() % 100));
            }

            // Monthly ranking
            List<MemberInviteStasticRank> topInviteMonth = memberInviteStasticService.topInviteCountByType(2, 20);
            for (MemberInviteStasticRank item4 : topInviteMonth) {
                if (!StringUtils.isEmpty(item4.getUserIdentify())) {
                    item4.setUserIdentify(item4.getUserIdentify().substring(0, 3) + "****"
                            + item4.getUserIdentify().substring(item4.getUserIdentify().length() - 4));
                }
                item4.setMemberId(item4.getMemberId() * (item4.getMemberId() % 100));
            }
            resultObj.put("topinviteweek", topInviteWeek);
            resultObj.put("topinvitemonth", topInviteMonth);

            valueOperations.set(SysConstant.MEMBER_PROMOTION_TOP_RANK + top, resultObj,
                    SysConstant.MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME, TimeUnit.SECONDS);
            return success(resultObj);
        }
    }

    @ApiOperation(value = "Query promotion records")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "Page number", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "Page size", defaultValue = "10"),
            @ApiImplicitParam(name = "time", value = "Filter by time: day, month, year, all", defaultValue = "all"),
    // @ApiImplicitParam(name = "level", value = "Filter by level: 1, 2, 3, all",
    // defaultValue = "all")
    })
    @PermissionOperation
    @GetMapping("/record")
    public MessageResult getPromotionRecords(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "time", defaultValue = "all") String time,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        IPage<RewardRecord> pageList = rewardRecordService.queryRewardPromotionPage(page, size, memberId);
        List<RewardRecord> recordList = pageList.getRecords();
        List<RewardRecordCommisionVO> results = new ArrayList<>();
        if (recordList.size() > 0) {
            List<Long> friendIds = new ArrayList<>();
            for (RewardRecord re : recordList) {
                if (StringUtils.hasText(re.getReferId()))
                    friendIds.add(Long.parseLong(re.getReferId()));
            }
            Map<Long, Member> memberMap = memberService.mapByMemberIds(friendIds);
            for (RewardRecord re : recordList) {
                if (!StringUtils.hasText(re.getReferId()) || !memberMap.containsKey(Long.parseLong(re.getReferId())))
                    continue;
                Member mem = memberMap.get(Long.parseLong(re.getReferId()));
                RewardRecordCommisionVO rel = new RewardRecordCommisionVO();
                rel.setUid(mem.getId());
                rel.setCreateTime(re.getCreateTime());
                rel.setType("Spot");
                rel.setBonus(re.getAmount());
                results.add(rel);
            }
        }

        MessageResult messageResult = MessageResult.success();
        messageResult.setData(results);
        messageResult.setTotalPage(pageList.getPages() + "");
        messageResult.setTotalElement(pageList.getTotal() + "");
        return messageResult;
    }

    /**
     * Query only referral rewards
     */
    @ApiOperation(value = "Query only referral rewards")
    @PermissionOperation
    @RequestMapping(value = "/reward/record")
    public MessageResult rewardRecord2(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        IPage<RewardRecord> pageList = rewardRecordService.queryRewardPromotionPage(pageNo, pageSize, member.getId());
        MessageResult result = MessageResult.success();
        List<RewardRecord> list = pageList.getRecords();
        result.setData(list.stream().map(x -> {
            Coin coin = coinService.findByUnit(x.getCoinId());
            return PromotionRewardRecord.builder()
                    .amount(x.getAmount())
                    .createTime(x.getCreateTime())
                    .remark(x.getRemark())
                    .symbol(coin.getUnit())
                    .build();
        }).collect(Collectors.toList()));

        result.setTotalPage(pageList.getPages() + "");
        result.setTotalElement(pageList.getTotal() + "");
        return result;
    }

    /**
     * Get free promotion card (BTC: 0.001)
     */
    @ApiOperation(value = "Get free promotion card")
    @PermissionOperation
    @RequestMapping(value = "/promotioncard/getfreecard")
    public MessageResult createFreeCard(@RequestHeader(SysConstant.SESSION_MEMBER) String member) {
        // Check real-name authentication
        AuthMember member1 = AuthMember.toAuthMember(member);
        Member authMember = memberService.getById(member1.getId());
        if (authMember.getMemberLevel() == MemberLevelEnum.GENERAL.getCode()) {
            return MessageResult.error(500, sourceService.getMessage("NO_REALNAME"));
        }
        // Check if already claimed once
        List<PromotionCard> result = promotionCardService.findAllByMemberIdAndIsFree(member1.getId(), 1);
        if (result != null && result.size() > 0) {
            return MessageResult.error(500, sourceService.getMessage("FREE_PROMO_CARD_REPEAT"));
        }

        PromotionCard card = new PromotionCard();
        card.setCardName("Partner Promotion Card");
        card.setCardNo(authMember.getPromotionCode() + GeneratorUtil.getNonceString(5).toUpperCase());
        card.setAmount(new BigDecimal(0.001));
        card.setCardDesc("");
        card.setCoinId("BTC");
        card.setCount(30);
        card.setMemberId(authMember.getId());
        card.setIsFree(1);
        card.setIsEnabled(1);
        card.setExchangeCount(0);
        card.setTotalAmount(new BigDecimal(0.03));
        card.setIsLock(0);
        card.setLockDays(0);
        card.setIsEnabled(1);
        card.setCreateTime(DateUtil.getCurrentDate());

        promotionCardService.save(card);

        return success(card);
    }

    /**
     * Get list of cards I created
     */
    @ApiOperation(value = "Get list of cards I created")
    @PermissionOperation
    @RequestMapping(value = "/promotioncard/mycard")
    public MessageResult getMyCardList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        List<PromotionCard> result = promotionCardService.findAllByMemberId(member.getId());
        return success(result);
    }

    /**
     * Card exchange details
     */
    @ApiOperation(value = "Card exchange details")
    @RequestMapping(value = "/promotioncard/detail")
    public MessageResult getCardDetail(@RequestParam(value = "cardId", defaultValue = "") Long cardId) {

        Assert.notNull(cardId, sourceService.getMessage("INVALID_EXCHANGE_CARD"));
        PromotionCard result = promotionCardService.getById(cardId);
        Assert.notNull(result, sourceService.getMessage("INVALID_EXCHANGE_CARD"));

        return success(result);
    }

    /**
     * Exchange card by code (Free cards can only be redeemed once)
     */
    @ApiOperation(value = "Card exchange details")
    @PermissionOperation
    @RequestMapping(value = "/promotioncard/exchangecard")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult exhcangeCard(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "cardNo", defaultValue = "") String cardNo) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        // Check if card exists
        Assert.notNull(cardNo, sourceService.getMessage("CARD_NUMBER_REQUIRED"));
        if (!StringUtils.hasText(cardNo)) {
            return error(sourceService.getMessage("CARD_NUMBER_REQUIRED"));
        }
        PromotionCard card = promotionCardService.findPromotionCardByCardNo(cardNo);
        Assert.notNull(card, sourceService.getMessage("INVALID_CARD_NUMBER"));

        // Check user existence
        Member authMember1 = memberService.getById(member.getId());
        Assert.notNull(authMember1, "Illegal operation!");

        // Check card validity
        if (card.getIsEnabled() == 0) {
            return error(sourceService.getMessage("ILLEGAL_OPERATION"));
        }

        // Check user wallet existence
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(card.getCoinId(),
                authMember1.getId());
        Assert.notNull(memberWallet, sourceService.getMessage("ASSET_NOT_FOUND"));

        // Check card quantity
        if (card.getExchangeCount() >= card.getCount()) {
            return error(sourceService.getMessage("PROMOTION_CARD_REDEEMED"));
        }
        // Check if already redeemed
        List<PromotionCardOrder> order = promotionCardOrderService.findByCardIdAndMemberId(card.getId(),
                authMember1.getId());
        if (order != null && order.size() > 0) {
            return error(sourceService.getMessage("PROMOTION_CARD_ALREADY_REDEEMED"));
        }

        // Check if already redeemed free card
        List<PromotionCardOrder> orderFree = promotionCardOrderService.findAllByMemberIdAndIsFree(authMember1.getId(),
                1);
        if (orderFree != null && orderFree.size() > 0) {
            return error(sourceService.getMessage("OFFICIAL_PROMO_CARD_ONCE"));
        }

        PromotionCardOrder newOrder = new PromotionCardOrder();
        newOrder.setMemberId(authMember1.getId());
        newOrder.setAmount(card.getAmount());
        newOrder.setCardId(card.getId());
        newOrder.setIsFree(card.getIsFree());
        newOrder.setIsLock(card.getIsLock());
        newOrder.setLockDays(card.getLockDays());
        newOrder.setState(1);
        newOrder.setCreateTime(DateUtil.getCurrentDate());
        promotionCardOrderService.save(newOrder);

        if (newOrder != null) {
            // If user has no inviter, add inviter
            if (authMember1.getInviterId() == null) {
                if (authMember1.getId() != card.getMemberId()) {
                    Member levelOneMember = memberService.getById(card.getMemberId());
                    // If user has real-name verification, save first & second level
                    authMember1.setInviterId(card.getMemberId());
                    if (authMember1.getMemberLevel() == MemberLevelEnum.REALNAME.getCode()) {
                        // First level
                        MemberPromotion one = new MemberPromotion();
                        one.setInviterId(card.getMemberId());
                        one.setInviteesId(authMember1.getId());
                        one.setLevel(0);
                        memberPromotionService.save(one);
                        levelOneMember.setFirstLevel(levelOneMember.getFirstLevel() + 1);

                        if (levelOneMember.getInviterId() != null) {
                            Member levelTwoMember = memberService.getById(levelOneMember.getInviterId());
                            // Second level
                            MemberPromotion two = new MemberPromotion();
                            two.setInviterId(levelTwoMember.getId());
                            two.setInviteesId(authMember1.getId());
                            two.setLevel(1);
                            memberPromotionService.save(two);
                            levelTwoMember.setSecondLevel(levelTwoMember.getSecondLevel() + 1);
                        }
                    }
                }
            }

            // Add asset to wallet
            memberWalletService.increaseFrozen(memberWallet.getId(), newOrder.getAmount());

            // Update main table
            card.setExchangeCount(card.getExchangeCount() + 1);
            promotionCardService.saveOrUpdate(card);

            return success(sourceService.getMessage("EXCHANGE_SUCCESS"));
        } else {
            return error(sourceService.getMessage("EXCHANGE_FAILED"));
        }
    }
}
