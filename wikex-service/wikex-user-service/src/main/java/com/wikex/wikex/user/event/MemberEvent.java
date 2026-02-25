package com.wikex.wikex.user.event;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.rewardHub.RewardHubHandler;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.util.BigDecimalUtils;
import com.wikex.wikex.util.DateUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class MemberEvent {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberPromotionService memberPromotionService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private MemberWeightUpperService memberWeightUpperService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RewardHubHandler rewardHandler;
    @Autowired
    private SettingService settingService;

    @Value("${commission.need.real-name:1}")
    private int needRealName;

    @Value("${commission.promotion.second-level:0}")
    private int promotionSecondLevel;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * member: user nhap ma gioi thieu
     * promotionCode: ma gioi thieu
     */
    public void onRegisterSuccess(Member member, String promotionCode, String lang) throws Exception {
        if (lang == null) {
            lang = "en_US";
        }
        JSONObject json = new JSONObject();
        json.put("uid", member.getId());

        rocketMQTemplate.convertAndSend("member-register", json.toJSONString());

        rocketMQTemplate.convertAndSend("member-register-swap", json.toJSONString());

        rocketMQTemplate.convertAndSend("member-register-coinswap", json.toJSONString());

        memberWeightUpperService.saveMemberWeightUpper(member);

        if (StringUtils.hasText(member.getEmail()))
            emailService.sentEmailWelcome(member.getEmail(), lang);

        List<String> arr_name = new ArrayList<>(Arrays.asList(
                SettingNameConstant.CAMPAIGN_ADD_POINT_REGISTER,
                SettingNameConstant.REGISTER_SUCCESS_EARN_BALANCE_USDT));
        Map<String, Settings> setting_dict = settingService.mapByArrNames(arr_name);

        if (setting_dict.containsKey(SettingNameConstant.CAMPAIGN_ADD_POINT_REGISTER)) {
            Settings setting = settingService.findByName(SettingNameConstant.CAMPAIGN_ADD_POINT_REGISTER);
            if (setting != null) {
                rewardHandler.saveCampaignMemberPoint(member.getId(), CampaignPointType.REGISTER.getCode(),
                        Integer.valueOf(setting.getValue().trim()), 0, 0, 0, 0);
            }
        }

        if (StringUtils.hasText(promotionCode)) {
            Member inviterMember = memberService.findMemberByPromotionCode(promotionCode);
            setMemberInviter(member, inviterMember);
        }
    }

    public void setMemberInviter(Member member, Member inviterMember) throws InterruptedException {
        if (inviterMember != null) {
            member.setInviterId(inviterMember.getId());
            memberService.updateById(member);
            countInviter(member, inviterMember);
            // memberPromotionService.addMemberPromotion(member.getId(),
            // inviterMember.getId());
            memberWeightUpperService.saveMemberWeightUpper(member);

            // if (needRealName == 0) {
            List<String> arr_name = new ArrayList<>(Arrays.asList(
                    SettingNameConstant.CAMPAIGN_ADD_POINT_INVITER_SUCCESS,
                    SettingNameConstant.CAMPAIGN_ADD_POINT_INVITER_SUCCESS_MAX));
            Map<String, Settings> setting_dict = settingService.mapByArrNames(arr_name);
            if (setting_dict.containsKey(SettingNameConstant.CAMPAIGN_ADD_POINT_INVITER_SUCCESS)) {
                Settings setting = setting_dict.get(SettingNameConstant.CAMPAIGN_ADD_POINT_INVITER_SUCCESS);
                Integer maxPoint = 10000;
                if (setting_dict.containsKey(SettingNameConstant.CAMPAIGN_ADD_POINT_INVITER_SUCCESS_MAX)) {
                    Settings setting_point_max = setting_dict
                            .get(SettingNameConstant.CAMPAIGN_ADD_POINT_INVITER_SUCCESS_MAX);
                    maxPoint = Integer.valueOf(setting_point_max.getValue().trim());
                }
                rewardHandler.saveCampaignMemberPoint(inviterMember.getId(), CampaignPointType.REFERRER.getCode(),
                        Integer.valueOf(setting.getValue().trim()), maxPoint, 0, 0, 0);
            }
            promotion(inviterMember, member);
            // }
        }
    }

    private void countInviter(Member member, Member member1) {

        member1.setFirstLevel(member1.getFirstLevel() + 1);
        memberService.updateById(member1);
        MemberPromotion one = new MemberPromotion();
        one.setInviterId(member1.getId());
        one.setInviteesId(member.getId());
        one.setLevel(0);
        one.setCreateTime(DateUtil.getCurrentDate());
        memberPromotionService.save(one);

        if (member1.getInviterId() != null) {
            Member member2 = memberService.getById(member1.getInviterId());
            member2.setSecondLevel(member2.getSecondLevel() + 1);
            memberService.updateById(member2);
            MemberPromotion two = new MemberPromotion();
            two.setInviterId(member2.getId());
            two.setInviteesId(member.getId());
            two.setLevel(1);
            two.setCreateTime(DateUtil.getCurrentDate());
            memberPromotionService.save(two);

            if (member2.getInviterId() != null) {
                Member member3 = memberService.getById(member2.getInviterId());
                member3.setThirdLevel(member3.getThirdLevel() + 1);
                memberService.updateById(member3);
                MemberPromotion three = new MemberPromotion();
                three.setInviterId(member3.getId());
                three.setInviteesId(member.getId());
                three.setLevel(2);
                three.setCreateTime(DateUtil.getCurrentDate());
                memberPromotionService.save(three);
            }
        }

    }

    public void onLoginSuccess(Member member, String ip) {

    }

    public void promotion(Member parent, Member member) {
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService
                .findByType(PromotionRewardType.REGISTER);
        if (rewardPromotionSetting == null)
            return;

        Coin coin = coinService.getById(rewardPromotionSetting.getCoinId());
        if (coin == null)
            return;

        MemberWallet memberWallet1 = memberWalletService.findByCoinUnitAndMemberId(coin.getUnit(), parent.getId());
        if (memberWallet1 == null) {
            memberWallet1 = new MemberWallet();
            memberWallet1.setCoinId(coin.getUnit());
            memberWallet1.setMemberId(parent.getId());
            memberWallet1.setBalance(new BigDecimal(0));
            memberWallet1.setFrozenBalance(new BigDecimal(0));
            memberWalletService.save(memberWallet1);
        }

        BigDecimal amount1 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one");
        if (amount1 == null || BigDecimalUtils.isEqual(amount1, BigDecimal.ZERO))
            return;

        memberWallet1.setBalance(BigDecimalUtils.add(memberWallet1.getBalance(), amount1));
        memberWalletService.updateById(memberWallet1);
        RewardRecord rewardRecord1 = new RewardRecord();
        rewardRecord1.setAmount(amount1);
        rewardRecord1.setCoinId(coin.getUnit());
        rewardRecord1.setMemberId(parent.getId());
        rewardRecord1.setRemark(rewardPromotionSetting.getType().getDescription());
        rewardRecord1.setType(RewardRecordType.PROMOTION);
        rewardRecord1.setCreateTime(new Date());
        rewardRecord1.setReferId(String.valueOf(member.getId()));
        rewardRecordService.save(rewardRecord1);

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount1);
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setType(TransactionType.PROMOTION_AWARD.getCode());
        memberTransaction.setMemberId(parent.getId());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransaction.setCreateTime(new Date());
        memberTransaction.setReferId(String.valueOf(member.getId()));
        memberTransactionService.save(memberTransaction);

        if (parent.getInviterId() != null) {
            Member member2 = memberService.getById(parent.getInviterId());
            if (member2 != null)
                promotionLevelTwo(rewardPromotionSetting, coin, member2, member);
        }
    }

    private void promotionLevelTwo(RewardPromotionSetting rewardPromotionSetting, Coin coin, Member member2,
            Member member) {
        if (rewardPromotionSetting == null)
            return;
        if (coin == null)
            return;

        MemberWallet memberWallet2 = memberWalletService.findByCoinUnitAndMemberId(coin.getUnit(), member2.getId());
        if (memberWallet2 == null) {
            memberWallet2 = new MemberWallet();
            memberWallet2.setCoinId(coin.getUnit());
            memberWallet2.setMemberId(member2.getId());
            memberWallet2.setBalance(new BigDecimal(0));
            memberWallet2.setFrozenBalance(new BigDecimal(0));
            memberWalletService.save(memberWallet2);
        }

        BigDecimal amount2 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two");
        if (amount2 == null || BigDecimalUtils.isEqual(amount2, BigDecimal.ZERO))
            return;

        memberWallet2.setBalance(BigDecimalUtils.add(memberWallet2.getBalance(), amount2));
        memberWalletService.updateById(memberWallet2);
        RewardRecord rewardRecord2 = new RewardRecord();
        rewardRecord2.setAmount(amount2);
        rewardRecord2.setCoinId(coin.getUnit());
        rewardRecord2.setMemberId(member2.getId());
        rewardRecord2.setRemark(rewardPromotionSetting.getType().getDescription());
        rewardRecord2.setType(RewardRecordType.PROMOTION);
        rewardRecord2.setCreateTime(new Date());
        rewardRecord2.setReferId(String.valueOf(member.getId()));
        rewardRecordService.save(rewardRecord2);

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount2);
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setType(TransactionType.PROMOTION_AWARD.getCode());
        memberTransaction.setMemberId(member2.getId());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransaction.setCreateTime(new Date());
        memberTransaction.setReferId(String.valueOf(member.getId()));
        memberTransactionService.save(memberTransaction);

        if (member2.getInviterId() != null) {
            Member member3 = memberService.getById(member2.getInviterId());
            if (member3 != null)
                promotionLevelThree(rewardPromotionSetting, coin, member3, member);
        }
    }

    private void promotionLevelThree(RewardPromotionSetting rewardPromotionSetting, Coin coin, Member member3,
            Member member) {
        if (rewardPromotionSetting == null)
            return;
        if (coin == null)
            return;

        MemberWallet wallet3 = memberWalletService.findByCoinUnitAndMemberId(coin.getUnit(), member3.getId());
        BigDecimal amount3 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("three");
        if (amount3 == null || BigDecimalUtils.isEqual(amount3, BigDecimal.ZERO))
            return;
        wallet3.setBalance(BigDecimalUtils.add(wallet3.getBalance(), amount3));
        memberWalletService.updateById(wallet3);

        RewardRecord rewardRecord3 = new RewardRecord();
        rewardRecord3.setAmount(amount3);
        rewardRecord3.setCoinId(coin.getUnit());
        rewardRecord3.setMemberId(member3.getId());
        rewardRecord3.setRemark(rewardPromotionSetting.getType().getDescription());
        rewardRecord3.setType(RewardRecordType.PROMOTION);
        rewardRecord3.setCreateTime(new Date());
        rewardRecord3.setReferId(String.valueOf(member.getId()));
        rewardRecordService.save(rewardRecord3);

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount3);
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setType(TransactionType.PROMOTION_AWARD.getCode());
        memberTransaction.setMemberId(member3.getId());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransaction.setCreateTime(new Date());
        memberTransaction.setReferId(String.valueOf(member.getId()));
        memberTransactionService.save(memberTransaction);
    }

}
