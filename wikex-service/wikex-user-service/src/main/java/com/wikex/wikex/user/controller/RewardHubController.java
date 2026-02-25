package com.wikex.wikex.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.CampaignBonusType;
import com.wikex.wikex.constant.RewardRecordType;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exchange.entity.CampaignSetting;
import com.wikex.wikex.exchange.entity.TradingVolumn;
import com.wikex.wikex.exchange.entity.TradingVolumnBonus;
import com.wikex.wikex.exchange.entity.TradingVolumnMonth;
import com.wikex.wikex.exchange.entity.TradingVolumnWeek;
import com.wikex.wikex.exchange.entity.LeaderboadardScoreWeek;
import com.wikex.wikex.exchange.entity.LeaderboardScoreMonth;
import com.wikex.wikex.exchange.entity.LeaderboardScore;
import com.wikex.wikex.exchange.entity.LeaderboadardCalendar;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.dto.CampaignDTO;
import com.wikex.wikex.user.dto.CampaignSettingExtraDataDTO;
import com.wikex.wikex.user.dto.CampaignTradeConfigDTO;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.rewardHub.RewardHubHandler;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.CampaignClaimRewardVO;
import com.wikex.wikex.user.vo.CampaignLeadboardVO;
import com.wikex.wikex.user.vo.CampaignRewardHubVO;
import com.wikex.wikex.user.vo.LeadboardScoreVO;
import com.wikex.wikex.user.vo.LeadboardCalendarVO;
import com.wikex.wikex.util.BigDecimalUtils;
import com.wikex.wikex.util.Convert;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;

import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;

@Api(tags = "Reward Hub")
@RestController
@RequestMapping("/reward-hub")
public class RewardHubController extends BaseController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private RewardRecordService rewardRecordService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private MySteryBoxService mySteryBoxService;

    @Autowired
    private RewardHubHandler rewardHandler;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private RewardAssetService rewardAssetService;

    @Autowired
    private RewardSharePostService rewardSharePostService;

    @Autowired
    private RewardPeriodTypeService rewardPeriodTypeService;

    @ApiOperation(value = "Get info hub")
    @PermissionOperation
    @GetMapping("info")
    public MessageResult infoHub(@RequestHeader(SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();
        Member mem = memberService.getById(memberId);
        CampaignRewardHubVO result = new CampaignRewardHubVO();
        result.setTotalReward(rewardHandler.sumRewardCampaignId(memberId));
        result.setAddress(mem.getRewardWalletAddress());
        result.setCurrentLevel(0);

        List<CampaignSetting> pointSettings = rewardHandler.findCampaignConfigs("level_point");
        if (pointSettings.size() > 0) {
            CampaignSetting campaign = pointSettings.get(0);
            CampaignSettingExtraDataDTO extraDataDTO = JSONObject.parseObject(campaign.getExtraData(),
                    CampaignSettingExtraDataDTO.class);
            if (extraDataDTO.getLevelConfigs().size() > 0) {
                Integer memberPoint = rewardHandler.sumCampaignMemberPoint(mem.getId());
                result.setCurrentPoint(memberPoint);

                CampaignSettingExtraDataDTO.LevelPoint level = extraDataDTO.getLevelConfigs().stream()
                        .filter(config -> config.getFrom() <= memberPoint && memberPoint < config.getTo())
                        .findFirst()
                        .orElse(null);

                Integer minPoint = extraDataDTO.getLevelConfigs().stream()
                        .map(CampaignSettingExtraDataDTO.LevelPoint::getFrom)
                        .min(Comparator.naturalOrder())
                        .orElse(0);
                Integer maxPoint = extraDataDTO.getLevelConfigs().stream()
                        .map(CampaignSettingExtraDataDTO.LevelPoint::getTo)
                        .max(Comparator.naturalOrder())
                        .orElse(100000000);

                if (level == null) {
                    if (memberPoint < minPoint) {
                        CampaignSettingExtraDataDTO.LevelPoint nextPoint = extraDataDTO.getLevelConfigs().stream()
                                .filter(config -> config.getFrom() > memberPoint)
                                .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.LevelPoint::getFrom))
                                .findFirst()
                                .orElse(null);
                        result.setNextPoint(nextPoint.getFrom());
                        result.setLevel(nextPoint.getLevel());
                    }
                    if (memberPoint >= maxPoint) {
                        CampaignSettingExtraDataDTO.LevelPoint nextPoint = extraDataDTO.getLevelConfigs().stream()
                                .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.LevelPoint::getFrom)
                                        .reversed())
                                .findFirst()
                                .orElse(null);
                        result.setCurrentLevel(nextPoint.getLevel());
                        result.setLevel(nextPoint.getLevel());
                        result.setNextPoint(nextPoint.getTo());
                    }
                } else {
                    result.setCurrentLevel(level.getLevel());
                    CampaignSettingExtraDataDTO.LevelPoint nextPoint = extraDataDTO.getLevelConfigs().stream()
                            .filter(config -> config.getLevel() > level.getLevel())
                            .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.LevelPoint::getFrom))
                            .findFirst()
                            .orElse(null);
                    if (nextPoint != null) {
                        result.setNextPoint(nextPoint.getFrom());
                        result.setLevel(nextPoint.getLevel());
                    } else {
                        result.setNextPoint(level.getFrom());
                        result.setLevel(level.getLevel());
                    }
                }
            } else {
                result.setLevel(0);
                result.setCurrentPoint(0);
                result.setNextPoint(0);
            }
        } else {
            result.setLevel(0);
            result.setCurrentPoint(0);
            result.setNextPoint(0);
        }
        return success(result);
    }

    @ApiOperation(value = "Get campaign authen")
    @PermissionOperation
    @GetMapping("campaign-info")
    public MessageResult findCampaignInfo(@RequestHeader(SESSION_MEMBER) String authMember) {
        Long memberId = 0L;
        if (StringUtils.hasText(authMember)) {
            AuthMember member = AuthMember.toAuthMember(authMember);
            memberId = member.getId();
        }
        List<CampaignSetting> campaigns = rewardHandler.findCampaignConfigs("volumn_trading");
        List<CampaignSetting> inviterConfigs = rewardHandler.findCampaignConfigs("inviter_count");

        CampaignDTO campaign_data = new CampaignDTO();
        if (campaigns.size() > 0) {
            CampaignSetting campaign = campaigns.get(0);
            CampaignSettingExtraDataDTO extraDataDTO = JSONObject.parseObject(campaign.getExtraData(),
                    CampaignSettingExtraDataDTO.class);

            if (extraDataDTO.getTradeConfigs().size() > 0) {
                CampaignTradeConfigDTO trading = new CampaignTradeConfigDTO();
                if (trading.getTradeProcess() == null) {
                    trading.setTradeProcess(new ArrayList<>());
                }
                BigDecimal maxVolumn = BigDecimal.ZERO;
                if (memberId == 0) {
                    trading.setTotalVolumn(BigDecimal.ZERO);
                } else {
                    List<Long> memberIds = new ArrayList<>();
                    memberIds.add(memberId);
                    BigDecimal totalVolumn = rewardHandler.sumVolumnByMemberIdsAndCampaignId(campaign.get_id(),
                            memberIds, CampaignBonusType.TRADING_VOLUMN.getCode());
                    trading.setTotalVolumn(totalVolumn);

                    List<TradingVolumnBonus> data_bonus = rewardHandler.findTradingVolumnBonus(campaign.get_id(),
                            memberId, CampaignBonusType.TRADING_VOLUMN.getCode());
                    if (data_bonus.size() > 0) {
                        Collections.sort(data_bonus, Comparator.comparing(TradingVolumnBonus::getVolumn));
                        for (int i = 0; i < data_bonus.size(); i++) {
                            TradingVolumnBonus bonus = data_bonus.get(i);
                            CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                            config.setId(bonus.get_id());
                            config.setVolumn(bonus.getVolumn());
                            config.setValue(bonus.getBonus());
                            config.setEarn(bonus.getBonus());
                            config.setClaimed(bonus.getClaimed());
                            trading.getTradeProcess().add(config);
                        }
                        maxVolumn = data_bonus.stream()
                                .map(TradingVolumnBonus::getVolumn)
                                .max(Comparator.naturalOrder())
                                .orElse(BigDecimal.ZERO);
                    }
                }
                Integer max_volumn = maxVolumn.intValue();
                List<CampaignSettingExtraDataDTO.TradeConfig> results = extraDataDTO.getTradeConfigs().stream()
                        .filter(config -> config.getKey() > max_volumn)
                        .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.TradeConfig::getKey))
                        .collect(Collectors.toList());
                for (int i = 0; i < results.size(); i++) {
                    CampaignSettingExtraDataDTO.TradeConfig bonus = results.get(i);
                    CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                    config.setVolumn(new BigDecimal(bonus.getKey()));
                    config.setValue(new BigDecimal(bonus.getValue()));
                    config.setEarn(BigDecimal.ZERO);
                    config.setClaimed(BigDecimal.ZERO);
                    trading.getTradeProcess().add(config);
                }
                campaign_data.setTrading(trading);
            }
            if (extraDataDTO.getFriendTradeConfigs().size() > 0) {
                CampaignTradeConfigDTO friendTrade = new CampaignTradeConfigDTO();
                if (friendTrade.getTradeProcess() == null) {
                    friendTrade.setTradeProcess(new ArrayList<>());
                }
                BigDecimal maxVolumn = BigDecimal.ZERO;
                if (memberId == 0) {
                    friendTrade.setTotalVolumn(BigDecimal.ZERO);
                } else {
                    List<Member> list_member = memberService.findPromotionMember(memberId);
                    BigDecimal totalVolumn = BigDecimal.ZERO;
                    if (list_member.size() > 0) {
                        List<Long> friend_ids = list_member.stream().map(Member::getId).collect(Collectors.toList());
                        totalVolumn = rewardHandler.sumVolumnByMemberIdsAndCampaignId(campaign.get_id(), friend_ids,
                                CampaignBonusType.TRADING_VOLUMN.getCode());
                    }
                    friendTrade.setTotalVolumn(totalVolumn);

                    List<TradingVolumnBonus> data_bonus = rewardHandler.findTradingVolumnBonus(campaign.get_id(),
                            memberId, CampaignBonusType.FRIEND_TRANDE.getCode());
                    if (data_bonus.size() > 0) {
                        Collections.sort(data_bonus, Comparator.comparing(TradingVolumnBonus::getVolumn));
                        for (int i = 0; i < data_bonus.size(); i++) {
                            TradingVolumnBonus bonus = data_bonus.get(i);
                            CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                            config.setId(bonus.get_id());
                            config.setVolumn(bonus.getVolumn());
                            config.setValue(bonus.getBonus());
                            config.setEarn(bonus.getBonus());
                            config.setClaimed(bonus.getClaimed());
                            friendTrade.getTradeProcess().add(config);
                        }
                        maxVolumn = data_bonus.stream()
                                .map(TradingVolumnBonus::getVolumn)
                                .max(Comparator.naturalOrder())
                                .orElse(BigDecimal.ZERO);
                    }
                }
                Integer max_volumn = maxVolumn.intValue();
                List<CampaignSettingExtraDataDTO.TradeConfig> results = extraDataDTO.getFriendTradeConfigs().stream()
                        .filter(config -> config.getKey() > max_volumn)
                        .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.TradeConfig::getKey))
                        .collect(Collectors.toList());
                for (int i = 0; i < results.size(); i++) {
                    CampaignSettingExtraDataDTO.TradeConfig bonus = results.get(i);
                    CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                    config.setVolumn(new BigDecimal(bonus.getKey()));
                    config.setValue(new BigDecimal(bonus.getValue()));
                    config.setEarn(BigDecimal.ZERO);
                    config.setClaimed(BigDecimal.ZERO);
                    friendTrade.getTradeProcess().add(config);
                }
                campaign_data.setFriendTrading(friendTrade);
            }
        }
        if (inviterConfigs.size() > 0) {
            CampaignSetting inviterConfig = inviterConfigs.get(0);
            CampaignSettingExtraDataDTO extraDataDTO = JSONObject.parseObject(inviterConfig.getExtraData(),
                    CampaignSettingExtraDataDTO.class);
            if (extraDataDTO.getInviterConfigs().size() > 0) {
                CampaignTradeConfigDTO inviterData = new CampaignTradeConfigDTO();
                if (inviterData.getTradeProcess() == null) {
                    inviterData.setTradeProcess(new ArrayList<>());
                }
                BigDecimal maxFriend = BigDecimal.ZERO;
                if (memberId == 0) {
                    inviterData.setTotalVolumn(BigDecimal.ZERO);
                } else {
                    List<Long> friend_ids = new ArrayList<>();
                    friend_ids.add(memberId);
                    BigDecimal totalChild = rewardHandler.sumVolumnByMemberIdsAndCampaignId(inviterConfig.get_id(),
                            friend_ids, CampaignBonusType.INVITER.getCode());
                    inviterData.setTotalVolumn(totalChild);

                    List<TradingVolumnBonus> data_bonus = rewardHandler.findTradingVolumnBonus(inviterConfig.get_id(),
                            memberId, CampaignBonusType.INVITER.getCode());
                    if (data_bonus.size() > 0) {
                        Collections.sort(data_bonus, Comparator.comparing(TradingVolumnBonus::getVolumn));
                        for (int i = 0; i < data_bonus.size(); i++) {
                            TradingVolumnBonus bonus = data_bonus.get(i);
                            CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                            config.setId(bonus.get_id());
                            config.setVolumn(bonus.getVolumn());
                            config.setValue(bonus.getBonus());
                            config.setEarn(bonus.getBonus());
                            config.setClaimed(bonus.getClaimed());
                            inviterData.getTradeProcess().add(config);
                        }
                        maxFriend = data_bonus.stream()
                                .map(TradingVolumnBonus::getVolumn)
                                .max(Comparator.naturalOrder())
                                .orElse(BigDecimal.ZERO);
                    }
                }
                Integer max_friend = maxFriend.intValue();
                List<CampaignSettingExtraDataDTO.TradeConfig> results = extraDataDTO.getInviterConfigs().stream()
                        .filter(config -> config.getKey() > max_friend)
                        .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.TradeConfig::getKey))
                        .collect(Collectors.toList());
                for (int i = 0; i < results.size(); i++) {
                    CampaignSettingExtraDataDTO.TradeConfig bonus = results.get(i);
                    CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                    config.setVolumn(new BigDecimal(bonus.getKey()));
                    config.setValue(new BigDecimal(bonus.getValue()));
                    config.setEarn(BigDecimal.ZERO);
                    config.setClaimed(BigDecimal.ZERO);
                    inviterData.getTradeProcess().add(config);
                }
                campaign_data.setInviter(inviterData);
            }
        }
        return success(campaign_data);
    }

    @ApiOperation(value = "Get campaign")
    @GetMapping("campaign")
    public MessageResult findCampaign() {
        Long memberId = 0L;
        List<CampaignSetting> campaigns = rewardHandler.findCampaignConfigs("volumn_trading");
        List<CampaignSetting> inviterConfigs = rewardHandler.findCampaignConfigs("inviter_count");

        CampaignDTO campaign_data = new CampaignDTO();
        if (campaigns.size() > 0) {
            CampaignSetting campaign = campaigns.get(0);
            CampaignSettingExtraDataDTO extraDataDTO = JSONObject.parseObject(campaign.getExtraData(),
                    CampaignSettingExtraDataDTO.class);

            if (extraDataDTO.getTradeConfigs().size() > 0) {
                CampaignTradeConfigDTO trading = new CampaignTradeConfigDTO();
                if (trading.getTradeProcess() == null) {
                    trading.setTradeProcess(new ArrayList<>());
                }
                BigDecimal maxVolumn = BigDecimal.ZERO;
                if (memberId == 0) {
                    trading.setTotalVolumn(BigDecimal.ZERO);
                } else {
                    List<Long> memberIds = new ArrayList<>();
                    memberIds.add(memberId);
                    BigDecimal totalVolumn = rewardHandler.sumVolumnByMemberIdsAndCampaignId(campaign.get_id(),
                            memberIds, CampaignBonusType.TRADING_VOLUMN.getCode());
                    trading.setTotalVolumn(totalVolumn);

                    List<TradingVolumnBonus> data_bonus = rewardHandler.findTradingVolumnBonus(campaign.get_id(),
                            memberId, CampaignBonusType.TRADING_VOLUMN.getCode());
                    if (data_bonus.size() > 0) {
                        Collections.sort(data_bonus, Comparator.comparing(TradingVolumnBonus::getVolumn));
                        for (int i = 0; i < data_bonus.size(); i++) {
                            TradingVolumnBonus bonus = data_bonus.get(i);
                            CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                            config.setId(bonus.get_id());
                            config.setVolumn(bonus.getVolumn());
                            config.setValue(bonus.getBonus());
                            config.setEarn(bonus.getBonus());
                            config.setClaimed(bonus.getClaimed());
                            trading.getTradeProcess().add(config);
                        }
                        maxVolumn = data_bonus.stream()
                                .map(TradingVolumnBonus::getVolumn)
                                .max(Comparator.naturalOrder())
                                .orElse(BigDecimal.ZERO);
                    }
                }
                Integer max_volumn = maxVolumn.intValue();
                List<CampaignSettingExtraDataDTO.TradeConfig> results = extraDataDTO.getTradeConfigs().stream()
                        .filter(config -> config.getKey() > max_volumn)
                        .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.TradeConfig::getKey))
                        .collect(Collectors.toList());
                for (int i = 0; i < results.size(); i++) {
                    CampaignSettingExtraDataDTO.TradeConfig bonus = results.get(i);
                    CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                    config.setVolumn(new BigDecimal(bonus.getKey()));
                    config.setValue(new BigDecimal(bonus.getValue()));
                    config.setEarn(BigDecimal.ZERO);
                    config.setClaimed(BigDecimal.ZERO);
                    trading.getTradeProcess().add(config);
                }
                campaign_data.setTrading(trading);
            }
            if (extraDataDTO.getFriendTradeConfigs().size() > 0) {
                CampaignTradeConfigDTO friendTrade = new CampaignTradeConfigDTO();
                if (friendTrade.getTradeProcess() == null) {
                    friendTrade.setTradeProcess(new ArrayList<>());
                }
                BigDecimal maxVolumn = BigDecimal.ZERO;
                if (memberId == 0) {
                    friendTrade.setTotalVolumn(BigDecimal.ZERO);
                } else {
                    List<Member> list_member = memberService.findPromotionMember(memberId);
                    BigDecimal totalVolumn = BigDecimal.ZERO;
                    if (list_member.size() > 0) {
                        List<Long> friend_ids = list_member.stream().map(Member::getId).collect(Collectors.toList());
                        totalVolumn = rewardHandler.sumVolumnByMemberIdsAndCampaignId(campaign.get_id(), friend_ids,
                                CampaignBonusType.TRADING_VOLUMN.getCode());
                    }
                    friendTrade.setTotalVolumn(totalVolumn);

                    List<TradingVolumnBonus> data_bonus = rewardHandler.findTradingVolumnBonus(campaign.get_id(),
                            memberId, CampaignBonusType.FRIEND_TRANDE.getCode());
                    if (data_bonus.size() > 0) {
                        Collections.sort(data_bonus, Comparator.comparing(TradingVolumnBonus::getVolumn));
                        for (int i = 0; i < data_bonus.size(); i++) {
                            TradingVolumnBonus bonus = data_bonus.get(i);
                            CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                            config.setId(bonus.get_id());
                            config.setVolumn(bonus.getVolumn());
                            config.setValue(bonus.getBonus());
                            config.setEarn(bonus.getBonus());
                            config.setClaimed(bonus.getClaimed());
                            friendTrade.getTradeProcess().add(config);
                        }
                        maxVolumn = data_bonus.stream()
                                .map(TradingVolumnBonus::getVolumn)
                                .max(Comparator.naturalOrder())
                                .orElse(BigDecimal.ZERO);
                    }
                }
                Integer max_volumn = maxVolumn.intValue();
                List<CampaignSettingExtraDataDTO.TradeConfig> results = extraDataDTO.getFriendTradeConfigs().stream()
                        .filter(config -> config.getKey() > max_volumn)
                        .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.TradeConfig::getKey))
                        .collect(Collectors.toList());
                for (int i = 0; i < results.size(); i++) {
                    CampaignSettingExtraDataDTO.TradeConfig bonus = results.get(i);
                    CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                    config.setVolumn(new BigDecimal(bonus.getKey()));
                    config.setValue(new BigDecimal(bonus.getValue()));
                    config.setEarn(BigDecimal.ZERO);
                    config.setClaimed(BigDecimal.ZERO);
                    friendTrade.getTradeProcess().add(config);
                }
                campaign_data.setFriendTrading(friendTrade);
            }
        }
        if (inviterConfigs.size() > 0) {
            CampaignSetting inviterConfig = inviterConfigs.get(0);
            CampaignSettingExtraDataDTO extraDataDTO = JSONObject.parseObject(inviterConfig.getExtraData(),
                    CampaignSettingExtraDataDTO.class);
            if (extraDataDTO.getInviterConfigs().size() > 0) {
                CampaignTradeConfigDTO inviterData = new CampaignTradeConfigDTO();
                if (inviterData.getTradeProcess() == null) {
                    inviterData.setTradeProcess(new ArrayList<>());
                }
                BigDecimal maxFriend = BigDecimal.ZERO;
                if (memberId == 0) {
                    inviterData.setTotalVolumn(BigDecimal.ZERO);
                } else {
                    List<Long> friend_ids = new ArrayList<>();
                    friend_ids.add(memberId);
                    BigDecimal totalChild = rewardHandler.sumVolumnByMemberIdsAndCampaignId(inviterConfig.get_id(),
                            friend_ids, CampaignBonusType.INVITER.getCode());
                    inviterData.setTotalVolumn(totalChild);

                    List<TradingVolumnBonus> data_bonus = rewardHandler.findTradingVolumnBonus(inviterConfig.get_id(),
                            memberId, CampaignBonusType.INVITER.getCode());
                    if (data_bonus.size() > 0) {
                        Collections.sort(data_bonus, Comparator.comparing(TradingVolumnBonus::getVolumn));
                        for (int i = 0; i < data_bonus.size(); i++) {
                            TradingVolumnBonus bonus = data_bonus.get(i);
                            CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                            config.setId(bonus.get_id());
                            config.setVolumn(bonus.getVolumn());
                            config.setValue(bonus.getBonus());
                            config.setEarn(bonus.getBonus());
                            config.setClaimed(bonus.getClaimed());
                            inviterData.getTradeProcess().add(config);
                        }
                        maxFriend = data_bonus.stream()
                                .map(TradingVolumnBonus::getVolumn)
                                .max(Comparator.naturalOrder())
                                .orElse(BigDecimal.ZERO);
                    }
                }
                Integer max_friend = maxFriend.intValue();
                List<CampaignSettingExtraDataDTO.TradeConfig> results = extraDataDTO.getInviterConfigs().stream()
                        .filter(config -> config.getKey() > max_friend)
                        .sorted(Comparator.comparing(CampaignSettingExtraDataDTO.TradeConfig::getKey))
                        .collect(Collectors.toList());
                for (int i = 0; i < results.size(); i++) {
                    CampaignSettingExtraDataDTO.TradeConfig bonus = results.get(i);
                    CampaignTradeConfigDTO.TradeConfig config = new CampaignTradeConfigDTO.TradeConfig();
                    config.setVolumn(new BigDecimal(bonus.getKey()));
                    config.setValue(new BigDecimal(bonus.getValue()));
                    config.setEarn(BigDecimal.ZERO);
                    config.setClaimed(BigDecimal.ZERO);
                    inviterData.getTradeProcess().add(config);
                }
                campaign_data.setInviter(inviterData);
            }
        }
        return success(campaign_data);
    }

    @ApiOperation(value = "Claim Earn")
    @PermissionOperation
    @PostMapping(value = "/claim")
    public MessageResult claimReward(@RequestHeader(SESSION_MEMBER) String authMember, String id) {
        if (!StringUtils.hasText(id))
            return MessageResult.error(500, msService.getMessage("PARAMETER_ERROR"));

        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();
        TradingVolumnBonus bonus = rewardHandler.findOneTradingVolumnBonus(id);
        if (bonus == null || bonus.getMemberId().intValue() != memberId
                || bonus.getBonus().equals(bonus.getClaimed())) {
            return MessageResult.error(500, msService.getMessage("PARAMETER_ERROR"));
        }
        rewardHandler.updateClaimBonus(id, bonus.getBonus());

        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId("USDT", memberId);
        memberWallet.setBalance(BigDecimalUtils.add(memberWallet.getBalance(), bonus.getBonus()));
        memberWalletService.updateById(memberWallet);

        RewardRecordType rewardType = RewardRecordType.CAMPAIGN_TRADING_VOLUMN_CLAIM;
        if (bonus.getType() == CampaignBonusType.FRIEND_TRANDE.getCode())
            rewardType = RewardRecordType.CAMPAIGN_TRADING_FRIEND_CLAIM;
        if (bonus.getType() == CampaignBonusType.INVITER.getCode())
            rewardType = RewardRecordType.CAMPAIGN_INVITER_CLAIM;
        if (bonus.getType() == CampaignBonusType.COMMISION_FEE.getCode())
            rewardType = RewardRecordType.CAMPAIGN_EXCHANGE_FEE;

        RewardRecord rewardRecord = new RewardRecord();
        rewardRecord.setAmount(bonus.getBonus());
        rewardRecord.setCoinId("USDT");
        rewardRecord.setMemberId(memberId);
        rewardRecord.setRemark(rewardType.getDescription());
        rewardRecord.setType(rewardType);
        rewardRecord.setCreateTime(new Date());
        rewardRecord.setReferId(String.valueOf(memberId));
        rewardRecordService.save(rewardRecord);

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(bonus.getBonus());
        memberTransaction.setSymbol("USDT");
        memberTransaction.setType(TransactionType.CAMPAIGN_CLAIM.getCode());
        memberTransaction.setMemberId(memberId);
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransaction.setCreateTime(new Date());
        memberTransaction.setReferId(String.valueOf(memberId));
        memberTransactionService.save(memberTransaction);

        return MessageResult.success();
    }

    @ApiOperation(value = "Get Friend Referral Overal")
    @GetMapping("inviter_overal")
    public MessageResult getInviterOveral() {
        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }

        List<TradingVolumn> weeks = rewardHandler.findTradingVolumn(CampaignBonusType.INVITER.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumn weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Friend Referral Ranking In Week")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "week")
    })
    @GetMapping("inviter_week")
    public MessageResult getInviterWeek(Integer week) {
        Integer weekOfYear = week;
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer year = utcDate.getYear();
        if (week == 0) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            weekOfYear = utcDate.get(weekFields.weekOfYear());
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }

        List<Integer> num_weeks = rewardHandler.getWeekNosByYear(year, CampaignBonusType.INVITER.getCode());
        for (int i = 0; i < num_weeks.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Week " + num_weeks.get(i));
            ar.setIndex(num_weeks.get(i));
            if (weekOfYear == num_weeks.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnWeek> weeks = rewardHandler.findTradingVolumnWeek(year, weekOfYear,
                CampaignBonusType.INVITER.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumnWeek weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Friend Referral Ranking In Month")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "month")
    })
    @GetMapping("inviter_month")
    public MessageResult getInviterMonth(Integer month) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer monthOfYear = month;
        Integer year = utcDate.getYear();
        if (month == 0) {
            monthOfYear = utcDate.getMonthValue();
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }

        List<Integer> num_months = rewardHandler.getMonthNosByYear(year, CampaignBonusType.INVITER.getCode());
        for (int i = 0; i < num_months.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Month " + num_months.get(i));
            ar.setIndex(num_months.get(i));
            if (monthOfYear == num_months.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnMonth> months = rewardHandler.findTradingVolumnMonth(year, monthOfYear,
                CampaignBonusType.INVITER.getCode());
        if (months.size() == 0)
            return success(results);

        List<Long> memberIds = months.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < months.size(); i++) {
            TradingVolumnMonth weekOjb = months.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Tradding Volumn Overal")
    @GetMapping("trading_overal")
    public MessageResult getTradingVolumnOveral() {
        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }

        List<TradingVolumn> weeks = rewardHandler.findTradingVolumn(CampaignBonusType.TRADING_VOLUMN.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumn weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Tradding Volumn In Week")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "week")
    })
    @GetMapping("trading_week")
    public MessageResult getTradingVolumnWeek(Integer week) {
        Integer weekOfYear = week;
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer year = utcDate.getYear();
        if (week == 0) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            weekOfYear = utcDate.get(weekFields.weekOfYear());
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }

        List<Integer> num_weeks = rewardHandler.getWeekNosByYear(year, CampaignBonusType.TRADING_VOLUMN.getCode());
        for (int i = 0; i < num_weeks.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Week " + num_weeks.get(i));
            ar.setIndex(num_weeks.get(i));
            if (weekOfYear == num_weeks.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnWeek> weeks = rewardHandler.findTradingVolumnWeek(year, weekOfYear,
                CampaignBonusType.TRADING_VOLUMN.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumnWeek weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Trading Volumn In Month")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "month")
    })
    @GetMapping("trading_month")
    public MessageResult getTradingVolumnMonth(Integer month) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer monthOfYear = month;
        Integer year = utcDate.getYear();
        if (month == 0) {
            monthOfYear = utcDate.getMonthValue();
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }

        List<Integer> num_months = rewardHandler.getMonthNosByYear(year, CampaignBonusType.TRADING_VOLUMN.getCode());
        for (int i = 0; i < num_months.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Month " + num_months.get(i));
            ar.setIndex(num_months.get(i));
            if (monthOfYear == num_months.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnMonth> months = rewardHandler.findTradingVolumnMonth(year, monthOfYear,
                CampaignBonusType.TRADING_VOLUMN.getCode());
        if (months.size() == 0)
            return success(results);

        List<Long> memberIds = months.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < months.size(); i++) {
            TradingVolumnMonth weekOjb = months.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Commison Volumn Overal")
    @GetMapping("commision_overal")
    public MessageResult getCommisonVolumnOveral() {
        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }

        List<TradingVolumn> weeks = rewardHandler.findTradingVolumn(CampaignBonusType.COMMISION_FEE.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumn weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Commison Volumn In Week")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "week")
    })
    @GetMapping("commision_week")
    public MessageResult getCommisonVolumnWeek(Integer week) {
        Integer weekOfYear = week;
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer year = utcDate.getYear();
        if (week == 0) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            weekOfYear = utcDate.get(weekFields.weekOfYear());
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }

        List<Integer> num_weeks = rewardHandler.getWeekNosByYear(year, CampaignBonusType.COMMISION_FEE.getCode());
        for (int i = 0; i < num_weeks.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Week " + num_weeks.get(i));
            ar.setIndex(num_weeks.get(i));
            if (weekOfYear == num_weeks.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnWeek> weeks = rewardHandler.findTradingVolumnWeek(year, weekOfYear,
                CampaignBonusType.COMMISION_FEE.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumnWeek weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());

                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Commison Volumn In Month")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "month")
    })
    @GetMapping("commision_month")
    public MessageResult getCommisionVolumnMonth(Integer month) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer monthOfYear = month;
        Integer year = utcDate.getYear();
        if (month == 0) {
            monthOfYear = utcDate.getMonthValue();
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }

        List<Integer> num_months = rewardHandler.getMonthNosByYear(year, CampaignBonusType.COMMISION_FEE.getCode());
        for (int i = 0; i < num_months.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Month " + num_months.get(i));
            ar.setIndex(num_months.get(i));
            if (monthOfYear == num_months.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnMonth> months = rewardHandler.findTradingVolumnMonth(year, monthOfYear,
                CampaignBonusType.COMMISION_FEE.getCode());
        if (months.size() == 0)
            return success(results);

        List<Long> memberIds = months.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < months.size(); i++) {
            TradingVolumnMonth weekOjb = months.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());

                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Pnl Amount Overal")
    @GetMapping("pnl_overal")
    public MessageResult getPnlOveral() {
        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }

        List<TradingVolumn> weeks = rewardHandler.findTradingVolumn(CampaignBonusType.PNL_AMOUNT.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumn weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Pnl Amount In Week")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "week")
    })
    @GetMapping("pnl_week")
    public MessageResult getPnlWeek(Integer week) {
        Integer weekOfYear = week;
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer year = utcDate.getYear();
        if (week == 0) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            weekOfYear = utcDate.get(weekFields.weekOfYear());
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }

        List<Integer> num_weeks = rewardHandler.getWeekNosByYear(year, CampaignBonusType.PNL_AMOUNT.getCode());
        for (int i = 0; i < num_weeks.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Week " + num_weeks.get(i));
            ar.setIndex(num_weeks.get(i));
            if (weekOfYear == num_weeks.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnWeek> weeks = rewardHandler.findTradingVolumnWeek(year, weekOfYear,
                CampaignBonusType.PNL_AMOUNT.getCode());
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < weeks.size(); i++) {
            TradingVolumnWeek weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get Pnl Amount In Month")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "month")
    })
    @GetMapping("pnl_month")
    public MessageResult getPnlMonth(Integer month) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer monthOfYear = month;
        Integer year = utcDate.getYear();
        if (month == 0) {
            monthOfYear = utcDate.getMonthValue();
        }

        CampaignLeadboardVO results = new CampaignLeadboardVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (results.getProcess() == null) {
            results.setProcess(new ArrayList<>());
        }

        List<Integer> num_months = rewardHandler.getMonthNosByYear(year, CampaignBonusType.PNL_AMOUNT.getCode());
        for (int i = 0; i < num_months.size(); i++) {
            CampaignLeadboardVO.obj ar = new CampaignLeadboardVO.obj();
            ar.setName("Month " + num_months.get(i));
            ar.setIndex(num_months.get(i));
            if (monthOfYear == num_months.get(i))
                ar.setActive(true);
            else
                ar.setActive(false);
            results.getProcess().add(ar);
        }
        Collections.sort(results.getProcess(), Comparator.comparing(CampaignLeadboardVO.obj::getIndex));

        List<TradingVolumnMonth> months = rewardHandler.findTradingVolumnMonth(year, monthOfYear,
                CampaignBonusType.PNL_AMOUNT.getCode());
        if (months.size() == 0)
            return success(results);

        List<Long> memberIds = months.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        for (int i = 0; i < months.size(); i++) {
            TradingVolumnMonth weekOjb = months.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                CampaignLeadboardVO.leadboard lead = new CampaignLeadboardVO.leadboard();
                lead.setRank(i + 1);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setRewardWallet(mem.getRewardWalletAddress());
                lead.setUid(mem.getId());
                lead.setTotal(weekOjb.getVolumn());
                results.getLeadboards().add(lead);
            }
        }
        Collections.sort(results.getLeadboards(), Comparator.comparing(CampaignLeadboardVO.leadboard::getRank));

        return success(results);
    }

    @ApiOperation(value = "Get History")
    @PermissionOperation
    @GetMapping("history")
    public MessageResult getHistoryClaim(@RequestHeader(SESSION_MEMBER) String authMember, Integer type) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        CampaignClaimRewardVO result = new CampaignClaimRewardVO();
        result.setTotal(rewardHandler.sumRewardCampaignId(memberId));
        result.setMyBoxUsdt(BigDecimal.ZERO);
        result.setMyBoxNft(0);
        MySteryBox box = mySteryBoxService.findAllByMemberId(memberId);
        if (box != null) {
            if (box.getType() == 1)
                result.setMyBoxUsdt(new BigDecimal(box.getValue()));
            else if (box.getType() == 2)
                result.setMyBoxNft(Integer.valueOf(box.getValue()));
        }

        List<RewardRecordType> types = new ArrayList<>();
        if (type == 0) {
            types.add(RewardRecordType.CAMPAIGN_TRADING_VOLUMN_CLAIM);
            types.add(RewardRecordType.CAMPAIGN_TRADING_FRIEND_CLAIM);
            types.add(RewardRecordType.CAMPAIGN_INVITER_CLAIM);
            types.add(RewardRecordType.CAMPAIGN_EXCHANGE_FEE);
        } else if (type == 3)
            types.add(RewardRecordType.CAMPAIGN_TRADING_VOLUMN_CLAIM);
        else if (type == 4)
            types.add(RewardRecordType.CAMPAIGN_TRADING_FRIEND_CLAIM);
        else if (type == 5)
            types.add(RewardRecordType.CAMPAIGN_INVITER_CLAIM);
        else if (type == 6)
            types.add(RewardRecordType.CAMPAIGN_EXCHANGE_FEE);

        List<RewardRecord> arr_bonus = rewardRecordService.getByMemberIdAndType(memberId, types);
        if (arr_bonus.size() > 0) {
            for (int i = 0; i < arr_bonus.size(); i++) {
                if (result.getHistories() == null) {
                    result.setHistories(new ArrayList<>());
                }
                RewardRecord bonus = arr_bonus.get(i);
                CampaignClaimRewardVO.history his = new CampaignClaimRewardVO.history();
                his.setType(bonus.getRemark());
                his.setDesc(bonus.getRemark() + " Claim reward");
                his.setAmount(bonus.getAmount());
                his.setStatus("Claimed");
                his.setTime(bonus.getCreateTime());
                result.getHistories().add(his);
            }
            Collections.sort(result.getHistories(),
                    Comparator.comparing(CampaignClaimRewardVO.history::getTime).reversed());
        }

        return success(result);
    }

    @ApiOperation(value = "Set Wallet Ear Reward")
    @PermissionOperation
    @PostMapping(value = "/set_wallet")
    public MessageResult setWalletAddress(@RequestHeader(SESSION_MEMBER) String authMember, String address) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();
        if (!StringUtils.hasText(address) || !address.matches("^0x[0-9a-fA-F]{40}$"))
            return MessageResult.error(500, msService.getMessage("WRONG_ADDRESS"));

        Member mem = memberService.getById(memberId);
        String existsAddress = mem.getRewardWalletAddress();
        if (StringUtils.hasText(existsAddress))
            return MessageResult.error(500, msService.getMessage("WALLET_ADDRESS_ALREADY_EXISTS"));

        if (memberService.walletAddressIsExist(address)) {
            return MessageResult.error(500, msService.getMessage("WALLET_ADDRESS_ALREADY_EXISTS"));
        }

        mem.setRewardWalletAddress(address);
        memberService.updateById(mem);

        return success();
    }

    @ApiOperation(value = "Claim Status")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "period", value = "week, month, overall"),
            @ApiImplicitParam(name = "periodNo", value = "periodNo"),
            @ApiImplicitParam(name = "yearNo", value = "2025, 2026")
    })
    @PermissionOperation
    @GetMapping("claim-status")
    public MessageResult claimStatus(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam("period") String periodStr,
            @RequestParam("periodNo") Integer periodNo,
            @RequestParam("yearNo") Integer yearNo) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        List<String> periods = new ArrayList<>();
        periods.add("week");
        periods.add("month");
        periods.add("overall");
        Map<String, Object> data = new HashMap<>();
        data.put("inTop", false);
        data.put("claimed", false);
        data.put("rank", null);

        if (!periods.contains(periodStr)) {
            MessageResult result = MessageResult.success("success");
            result.setData(data);
            return result;
        }

        List<RewardPeriodType> selectedList = rewardPeriodTypeService.findAll(periodStr, yearNo, periodNo);
        if (selectedList == null || selectedList.isEmpty()) {
            MessageResult result = MessageResult.success("success");
            result.setData(data);
            return result;
        }
        RewardPeriodType rewardIndex = selectedList.get(0);

        if (periodStr.equals("week")) {
            List<LeaderboadardScoreWeek> member_weeks = rewardHandler.findLeaderboadardScoreWeek(yearNo, periodNo, memberId);
            if (member_weeks.size() > 0) {
                LeaderboadardScoreWeek member_week = member_weeks.get(0);
                if (member_week.getRank() > rewardIndex.getType()) {
                    MessageResult result = MessageResult.success("success");
                    result.setData(data);
                    return result;
                }
                data.put("inTop", true);
                data.put("claimed", false);
                data.put("rank", member_week.getRank());
            }
        } else if (periodStr.equals("month")) {
            List<LeaderboardScoreMonth> member_months = rewardHandler.findLeaderboadardScoreMonth(yearNo, periodNo, memberId);
            if (member_months.size() > 0) {
                LeaderboardScoreMonth member_month = member_months.get(0);
                if (member_month.getRank() > rewardIndex.getType()) {
                    MessageResult result = MessageResult.success("success");
                    result.setData(data);
                    return result;
                }
                data.put("inTop", true);
                data.put("claimed", false);
                data.put("rank", member_month.getRank());
            }
        } else if (periodStr.equals("overall")) {
            List<LeaderboardScore> member_overalls = rewardHandler.findLeaderboadardScore(memberId);
            if (member_overalls.size() > 0) {
                LeaderboardScore member_overall = member_overalls.get(0);
                if (member_overall.getRank() > rewardIndex.getType()) {
                    MessageResult result = MessageResult.success("success");
                    result.setData(data);
                    return result;
                }
                data.put("inTop", true);
                data.put("claimed", false);
                data.put("rank", member_overall.getRank());
            }
        }

        RewardSharePost sharePost = rewardSharePostService.findOne(memberId, yearNo, periodStr, periodNo);
        boolean claimed = sharePost != null && sharePost.getStatus() != null && sharePost.getStatus() == 1;
        data.put("claimed", claimed);

        MessageResult result = MessageResult.success("success");
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Share")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "period", value = "week, month, overall"),
            @ApiImplicitParam(name = "periodNo", value = "periodNo"),
            @ApiImplicitParam(name = "yearNo", value = "2025, 2026")
    })
    @PermissionOperation
    @GetMapping("/share")
    public MessageResult share(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam(value = "period") String periodStr,
            @RequestParam(value = "periodNo") Integer periodNo,
            @RequestParam(value = "yearNo") Integer yearNo) {

        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        List<String> periods = new ArrayList<>();
        periods.add("week");
        periods.add("month");
        periods.add("overall");
        if (!periods.contains(periodStr)) {
            return MessageResult.error(400, "Invalid period");
        }

        List<RewardPeriodType> selectedList = rewardPeriodTypeService.findAll(periodStr, yearNo, periodNo);
        if (selectedList == null || selectedList.isEmpty()) {
            return MessageResult.error(404, msService.getMessage("RECORD_NOT_FOUND"));
        }
        RewardPeriodType rewardIndex = selectedList.get(0);

        Long rank = 0L;
        if (periodStr.equals("week")) {
            List<LeaderboadardScoreWeek> member_weeks = rewardHandler.findLeaderboadardScoreWeek(yearNo, periodNo, memberId);
            if (member_weeks.size() > 0) {
                LeaderboadardScoreWeek member_week = member_weeks.get(0);
                if (member_week.getRank() <= rewardIndex.getType()) {
                    rank = member_week.getRank();
                }
            }
        } else if (periodStr.equals("month")) {
            List<LeaderboardScoreMonth> member_months = rewardHandler.findLeaderboadardScoreMonth(yearNo, periodNo, memberId);
            if (member_months.size() > 0) {
                LeaderboardScoreMonth member_month = member_months.get(0);
                if (member_month.getRank() <= rewardIndex.getType()) {
                    rank = member_month.getRank();
                }
            }
        } else if (periodStr.equals("overall")) {
            List<LeaderboardScore> member_overalls = rewardHandler.findLeaderboadardScore(memberId);
            if (member_overalls.size() > 0) {
                LeaderboardScore member_overall = member_overalls.get(0);
                if (member_overall.getRank() <= rewardIndex.getType()) {
                    rank = member_overall.getRank();
                }
            }
        }

        if (rank == 0) {
            return MessageResult.error(404, msService.getMessage("RECORD_NOT_FOUND"));
        }

        RewardAsset asset = rewardAssetService.findByTypePeriodRank(periodStr, rank.intValue());
        if (asset == null) {
            return MessageResult.error(404, msService.getMessage("RECORD_NOT_FOUND"));
        }

        String imageUrl = asset.getImageUrl();
        if (!StringUtils.hasText(imageUrl)) {
            return MessageResult.error(404, msService.getMessage("IMAGE_NOT_FOUND"));
        }

        // Tạo hoặc lấy RewardSharePost
        RewardSharePost post = rewardSharePostService.findOne(memberId, yearNo, periodStr, periodNo);
        if (post == null) {
            post = new RewardSharePost();
            post.setMemberId(memberId);
            post.setType(rewardIndex.getType());
            post.setPeriod(periodStr);
            post.setPeriodNo(periodNo);
            post.setYearNo(yearNo);
            post.setStatus(0);
            post.setCreateTime(new Date());
            rewardSharePostService.save(post);
        }

        Member mem = memberService.getById(memberId);
        String refCode = mem.getPromotionCode();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("uid", memberId);
        data.put("imageUrl", imageUrl);
        data.put("refCode", refCode);
        data.put("postId", post.getId());

        MessageResult result = MessageResult.success("success");
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Submit Share Post Link")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId"),
            @ApiImplicitParam(name = "postUrl")
    })
    @PermissionOperation
    @PostMapping("submit-post")
    public MessageResult submitSharePost(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam("postId") Long postId,
            @RequestParam("postUrl") String postUrl) {
        if (!StringUtils.hasText(postUrl)) {
            return MessageResult.error(500, msService.getMessage("URL_REQUIRED"));
        }
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        RewardSharePost post = rewardSharePostService.getById(postId);

        if (post == null) {
            return MessageResult.error(404, msService.getMessage("POST_NOT_FOUND"));
        }

        if (!Objects.equals(post.getMemberId(), memberId)) {
            return MessageResult.error(403, msService.getMessage("ACCESS_DENIED"));
        }

        post.setPostUrl(postUrl);
        post.setStatus(1);
        rewardSharePostService.updateById(post);
        return MessageResult.success();
    }

    @ApiOperation(value = "Get Leaderboard Week Required Login")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "week", value = "Week Of Year"),
            @ApiImplicitParam(name = "year", value = "Year")
    })
    @PermissionOperation
    @RequestMapping("leaderboard_week_auth")
    public MessageResult getLeaderboardWeekAuth(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer year) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer weekOfYear = week;
        Integer yearNo = year;
        if (yearNo == null || yearNo == 0) {
            yearNo = utcDate.getYear();
        }
        if (weekOfYear == null || weekOfYear == 0) {
            weekOfYear = rewardHandler.maxWeekNoLeaderboard(yearNo);
            if (weekOfYear == null || weekOfYear == 0) {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                weekOfYear = utcDate.get(weekFields.weekOfYear());
            }
        }

        Long memberId = 0L;
        if (StringUtils.hasText(authMember)) {
            try {
                AuthMember member = AuthMember.toAuthMember(authMember);
                if (member != null)
                    memberId = member.getId();
            } catch (Exception e) {
                // Ignore parsing errors for optional authMember
            }
        }

        List<LeaderboadardScoreWeek> weeks = rewardHandler.findLeaderboadardScoreWeek(yearNo, weekOfYear, 0L);

        LeadboardScoreVO results = new LeadboardScoreVO();
        results.setWeek(weekOfYear);
        results.setYear(yearNo);
        results.setTimeRange(DateUtil.getBeginEndWeek(yearNo, weekOfYear));
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        if (memberId > 0L) {
            Set<Long> memberIdSet = new HashSet<>(memberIds);
            boolean exists = memberIdSet.contains(memberId);
            if (!exists) {
                List<LeaderboadardScoreWeek> member_weeks = rewardHandler.findLeaderboadardScoreWeek(yearNo, weekOfYear,
                        memberId);
                if (member_weeks.size() > 0) {
                    LeaderboadardScoreWeek member_week = member_weeks.get(0);
                    weeks.add(member_week);
                    memberIds.add(memberId);
                }
            }
        }

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        int rank = 1;
        for (int i = 0; i < weeks.size(); i++) {
            LeaderboadardScoreWeek weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                LeadboardScoreVO.leadboard lead = new LeadboardScoreVO.leadboard();
                lead.setRank(rank);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setUid(mem.getId());
                lead.setPnl(weekOjb.getPnl());
                lead.setReferral(weekOjb.getReferral());
                lead.setVolumn(weekOjb.getVolumn());
                lead.setCommision(weekOjb.getCommision());
                lead.setPoint(weekOjb.getPoint());
                results.getLeadboards().add(lead);
                rank += 1;
            }
        }
        Collections.sort(results.getLeadboards(),
                Comparator.comparing(LeadboardScoreVO.leadboard::getPoint).reversed());

        return success(results);
    }

    @ApiOperation(value = "Get Leaderboard Week Not Authen")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "week", value = "Week Of Year"),
            @ApiImplicitParam(name = "year", value = "Year")
    })
    @RequestMapping("leaderboard_week_public")
    public MessageResult getLeaderboardWeek(@RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer year) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer weekOfYear = week;
        Integer yearNo = year;
        if (yearNo == null || yearNo == 0) {
            yearNo = utcDate.getYear();
        }
        if (weekOfYear == null || weekOfYear == 0) {
            weekOfYear = rewardHandler.maxWeekNoLeaderboard(yearNo);
            if (weekOfYear == null || weekOfYear == 0) {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                weekOfYear = utcDate.get(weekFields.weekOfYear());
            }
        }

        Long memberId = 0L;
        List<LeaderboadardScoreWeek> weeks = rewardHandler.findLeaderboadardScoreWeek(yearNo, weekOfYear, 0L);

        LeadboardScoreVO results = new LeadboardScoreVO();
        results.setWeek(weekOfYear);
        results.setYear(yearNo);
        results.setTimeRange(DateUtil.getBeginEndWeek(yearNo, weekOfYear));
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (weeks.size() == 0)
            return success(results);

        List<Long> memberIds = weeks.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        if (memberId > 0L) {
            Set<Long> memberIdSet = new HashSet<>(memberIds);
            boolean exists = memberIdSet.contains(memberId);
            if (!exists) {
                List<LeaderboadardScoreWeek> member_weeks = rewardHandler.findLeaderboadardScoreWeek(yearNo, weekOfYear,
                        memberId);
                if (member_weeks.size() > 0) {
                    LeaderboadardScoreWeek member_week = member_weeks.get(0);
                    weeks.add(member_week);
                    memberIds.add(memberId);
                }
            }
        }

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        int rank = 1;
        for (int i = 0; i < weeks.size(); i++) {
            LeaderboadardScoreWeek weekOjb = weeks.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                LeadboardScoreVO.leadboard lead = new LeadboardScoreVO.leadboard();
                lead.setRank(rank);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setUid(mem.getId());
                lead.setPnl(weekOjb.getPnl());
                lead.setReferral(weekOjb.getReferral());
                lead.setVolumn(weekOjb.getVolumn());
                lead.setCommision(weekOjb.getCommision());
                lead.setPoint(weekOjb.getPoint());
                results.getLeadboards().add(lead);
                rank += 1;
            }
        }
        Collections.sort(results.getLeadboards(),
                Comparator.comparing(LeadboardScoreVO.leadboard::getPoint).reversed());

        return success(results);
    }

    @ApiOperation(value = "Get Leaderboard Month Required Login")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "month", value = "Month Of Year"),
            @ApiImplicitParam(name = "year", value = "Year")
    })
    @PermissionOperation
    @RequestMapping("leaderboard_month_auth")
    public MessageResult getLeaderboardMonthAuth(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer monthOfYear = month;
        Integer yearNo = year;
        if (yearNo == null || yearNo == 0) {
            yearNo = utcDate.getYear();
        }
        if (monthOfYear == null || monthOfYear == 0) {
            monthOfYear = rewardHandler.maxMonthNoLeaderboard(yearNo);
            if (monthOfYear == null || monthOfYear == 0) {
                monthOfYear = utcDate.getMonthValue();
            }
        }

        Long memberId = 0L;
        if (StringUtils.hasText(authMember)) {
            try {
                AuthMember member = AuthMember.toAuthMember(authMember);
                if (member != null)
                    memberId = member.getId();
            } catch (Exception e) {
                // Ignore parsing errors for optional authMember
            }
        }

        List<LeaderboardScoreMonth> months = rewardHandler.findLeaderboadardScoreMonth(yearNo, monthOfYear, 0L);

        LeadboardScoreVO results = new LeadboardScoreVO();
        results.setMonth(monthOfYear);
        results.setYear(yearNo);
        results.setTimeRange(DateUtil.getBeginEndMonth(yearNo, monthOfYear));
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (months.size() == 0)
            return success(results);

        List<Long> memberIds = months.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        if (memberId > 0L) {
            Set<Long> memberIdSet = new HashSet<>(memberIds);
            boolean exists = memberIdSet.contains(memberId);
            if (!exists) {
                List<LeaderboardScoreMonth> member_months = rewardHandler.findLeaderboadardScoreMonth(yearNo,
                        monthOfYear, memberId);
                if (member_months.size() > 0) {
                    LeaderboardScoreMonth member_week = member_months.get(0);
                    months.add(member_week);
                    memberIds.add(memberId);
                }
            }
        }

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        int rank = 1;
        for (int i = 0; i < months.size(); i++) {
            LeaderboardScoreMonth weekOjb = months.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                LeadboardScoreVO.leadboard lead = new LeadboardScoreVO.leadboard();
                lead.setRank(rank);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setUid(mem.getId());
                lead.setPnl(weekOjb.getPnl());
                lead.setReferral(weekOjb.getReferral());
                lead.setVolumn(weekOjb.getVolumn());
                lead.setCommision(weekOjb.getCommision());
                lead.setPoint(weekOjb.getPoint());
                results.getLeadboards().add(lead);
                rank += 1;
            }
        }
        Collections.sort(results.getLeadboards(),
                Comparator.comparing(LeadboardScoreVO.leadboard::getPoint).reversed());

        return success(results);
    }

    @ApiOperation(value = "Get Leaderboard Month Not Authen")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "month", value = "Month Of Year"),
            @ApiImplicitParam(name = "year", value = "Year")
    })
    @RequestMapping("leaderboard_month_public")
    public MessageResult getLeaderboardMonth(@RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Integer monthOfYear = month;
        Integer yearNo = year;
        if (yearNo == null || yearNo == 0) {
            yearNo = utcDate.getYear();
        }
        if (monthOfYear == null || monthOfYear == 0) {
            monthOfYear = rewardHandler.maxMonthNoLeaderboard(yearNo);
            if (monthOfYear == null || monthOfYear == 0) {
                monthOfYear = utcDate.getMonthValue();
            }
        }

        Long memberId = 0L;
        List<LeaderboardScoreMonth> months = rewardHandler.findLeaderboadardScoreMonth(yearNo, monthOfYear, 0L);

        LeadboardScoreVO results = new LeadboardScoreVO();
        results.setMonth(monthOfYear);
        results.setYear(yearNo);
        results.setTimeRange(DateUtil.getBeginEndMonth(yearNo, monthOfYear));
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (months.size() == 0)
            return success(results);

        List<Long> memberIds = months.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        if (memberId > 0L) {
            Set<Long> memberIdSet = new HashSet<>(memberIds);
            boolean exists = memberIdSet.contains(memberId);
            if (!exists) {
                List<LeaderboardScoreMonth> member_months = rewardHandler.findLeaderboadardScoreMonth(yearNo,
                        monthOfYear, memberId);
                if (member_months.size() > 0) {
                    LeaderboardScoreMonth member_week = member_months.get(0);
                    months.add(member_week);
                    memberIds.add(memberId);
                }
            }
        }

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        int rank = 1;
        for (int i = 0; i < months.size(); i++) {
            LeaderboardScoreMonth weekOjb = months.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                LeadboardScoreVO.leadboard lead = new LeadboardScoreVO.leadboard();
                lead.setRank(rank);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setUid(mem.getId());
                lead.setPnl(weekOjb.getPnl());
                lead.setReferral(weekOjb.getReferral());
                lead.setVolumn(weekOjb.getVolumn());
                lead.setCommision(weekOjb.getCommision());
                lead.setPoint(weekOjb.getPoint());
                results.getLeadboards().add(lead);
                rank += 1;
            }
        }
        Collections.sort(results.getLeadboards(),
                Comparator.comparing(LeadboardScoreVO.leadboard::getPoint).reversed());

        return success(results);
    }

    @ApiOperation(value = "Get Leaderboard Overall Required Login")
    @PermissionOperation
    @RequestMapping("leaderboard_overall_auth")
    public MessageResult getLeaderboardOverallAuth(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        Long memberId = 0L;
        if (StringUtils.hasText(authMember)) {
            try {
                AuthMember member = AuthMember.toAuthMember(authMember);
                if (member != null)
                    memberId = member.getId();
            } catch (Exception e) {
                // Ignore parsing errors for optional authMember
            }
        }

        List<LeaderboardScore> overalls = rewardHandler.findLeaderboadardScore(0L);

        LeadboardScoreVO results = new LeadboardScoreVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (overalls.size() == 0)
            return success(results);

        List<Long> memberIds = overalls.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        if (memberId > 0L) {
            Set<Long> memberIdSet = new HashSet<>(memberIds);
            boolean exists = memberIdSet.contains(memberId);
            if (!exists) {
                List<LeaderboardScore> member_scores = rewardHandler.findLeaderboadardScore(memberId);
                if (member_scores.size() > 0) {
                    LeaderboardScore member_week = member_scores.get(0);
                    overalls.add(member_week);
                    memberIds.add(memberId);
                }
            }
        }

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        int rank = 1;
        for (int i = 0; i < overalls.size(); i++) {
            LeaderboardScore weekOjb = overalls.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                LeadboardScoreVO.leadboard lead = new LeadboardScoreVO.leadboard();
                lead.setRank(rank);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setUid(mem.getId());
                lead.setPnl(weekOjb.getPnl());
                lead.setReferral(weekOjb.getReferral());
                lead.setVolumn(weekOjb.getVolumn());
                lead.setCommision(weekOjb.getCommision());
                lead.setPoint(weekOjb.getPoint());
                results.getLeadboards().add(lead);
                rank += 1;
            }
        }
        Collections.sort(results.getLeadboards(),
                Comparator.comparing(LeadboardScoreVO.leadboard::getPoint).reversed());

        return success(results);
    }

    @ApiOperation(value = "Get Leaderboard Overall Not Authen")
    @RequestMapping("leaderboard_overall_public")
    public MessageResult getLeaderboardOverall() {
        Long memberId = 0L;
        List<LeaderboardScore> overalls = rewardHandler.findLeaderboadardScore(0L);

        LeadboardScoreVO results = new LeadboardScoreVO();
        if (results.getLeadboards() == null) {
            results.setLeadboards(new ArrayList<>());
        }
        if (overalls.size() == 0)
            return success(results);

        List<Long> memberIds = overalls.stream()
                .map(o -> o.getMemberId().longValue())
                .collect(Collectors.toList());

        if (memberId > 0L) {
            Set<Long> memberIdSet = new HashSet<>(memberIds);
            boolean exists = memberIdSet.contains(memberId);
            if (!exists) {
                List<LeaderboardScore> member_scores = rewardHandler.findLeaderboadardScore(memberId);
                if (member_scores.size() > 0) {
                    LeaderboardScore member_week = member_scores.get(0);
                    overalls.add(member_week);
                    memberIds.add(memberId);
                }
            }
        }

        Map<Long, Member> member_dict = memberService.mapByMemberIds(memberIds);
        int rank = 1;
        for (int i = 0; i < overalls.size(); i++) {
            LeaderboardScore weekOjb = overalls.get(i);
            if (member_dict.containsKey(weekOjb.getMemberId().longValue())) {
                Member mem = member_dict.get(weekOjb.getMemberId().longValue());
                LeadboardScoreVO.leadboard lead = new LeadboardScoreVO.leadboard();
                lead.setRank(rank);
                lead.setUsername(Convert.dotedCharacters(mem.getUsername(), 5));
                lead.setUid(mem.getId());
                lead.setPnl(weekOjb.getPnl());
                lead.setReferral(weekOjb.getReferral());
                lead.setVolumn(weekOjb.getVolumn());
                lead.setCommision(weekOjb.getCommision());
                lead.setPoint(weekOjb.getPoint());
                results.getLeadboards().add(lead);
                rank += 1;
            }
        }
        Collections.sort(results.getLeadboards(),
                Comparator.comparing(LeadboardScoreVO.leadboard::getPoint).reversed());

        return success(results);
    }

    @ApiOperation(value = "Get Leaderboard Calendar")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "Year")
    })
    @RequestMapping("leaderboard_calendar")
    public MessageResult getWeekMonthInYear(@RequestParam(required = false) Integer year) {
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        if (year == null || year == 0)
            year = utcDate.getYear();
        LeadboardCalendarVO results = new LeadboardCalendarVO();
        results.setYear(year);

        List<LeaderboadardCalendar> dataMonths = rewardHandler.getLeaderboardCalendar(year, false);
        List<Integer> months = dataMonths.stream()
                .map(LeaderboadardCalendar::getMonthNo)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        List<LeaderboadardCalendar> dataWeeks = rewardHandler.getLeaderboardCalendar(year, true);
        List<Integer> weeks = dataWeeks.stream()
                .map(LeaderboadardCalendar::getWeekNo)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        results.setMonths(months);
        results.setWeeks(weeks);
        return success(results);
    }
}
