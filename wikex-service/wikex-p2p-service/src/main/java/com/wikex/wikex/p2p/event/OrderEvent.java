package com.wikex.wikex.p2p.event;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.PromotionRewardType;
import com.wikex.wikex.constant.RewardRecordType;
import com.wikex.wikex.p2p.config.CoinExchangeFactory;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.RewardPromotionSetting;
import com.wikex.wikex.user.entity.RewardRecord;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.feign.RewardPromotionSettingFeign;
import com.wikex.wikex.user.feign.RewardRecordFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.wikex.wikex.util.BigDecimalUtils.*;



@Service
public class OrderEvent {
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private RewardRecordFeign rewardRecordService;
    @Autowired
    private RewardPromotionSettingFeign rewardPromotionSettingService;
    @Autowired
    private CoinExchangeFactory coins;
    @Autowired
    private OtcCoinService otcCoinService;

    public void onOrderCompleted(OtcOrder order) {
        Member member = memberFeign.findMemberById(order.getMemberId());
        member.setTransactions(member.getTransactions() + 1);
        Member member1 = memberFeign.findMemberById(order.getCustomerId());
        member1.setTransactions(member1.getTransactions() + 1);
        memberFeign.save(member);
        memberFeign.save(member1);
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.TRANSACTION.getCode());
        if (rewardPromotionSetting != null) {
            Member[] array = {member, member1};
            Arrays.stream(array).forEach(
                    x -> {
                        
                        
                        
                        if (x.getTransactions() == 1 && x.getInviterId() != null) {
                            Member member2 = memberFeign.findMemberById(x.getInviterId());
                            MemberWallet memberWallet1 = memberWalletService.findByCoinUnitAndMemberId(rewardPromotionSetting.getCoinId(), member2.getId());


                            BigDecimal amount1 = mulRound(order.getNumber(), getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one")));
                            memberWallet1.setBalance(add(memberWallet1.getBalance(), amount1));
                            memberWalletService.save(memberWallet1);
                            RewardRecord rewardRecord1 = new RewardRecord();
                            rewardRecord1.setAmount(amount1);
                            rewardRecord1.setCoinId(rewardPromotionSetting.getCoinId());
                            rewardRecord1.setMemberId(member2.getId());
                            rewardRecord1.setRemark(rewardPromotionSetting.getType().getDescription());
                            rewardRecord1.setType(RewardRecordType.PROMOTION);
                            rewardRecordService.save(rewardRecord1);
                            if (member2.getInviterId() != null) {
                                Member member3 = memberFeign.findMemberById(member2.getInviterId());
                                MemberWallet memberWallet2 = memberWalletService.findByCoinUnitAndMemberId(rewardPromotionSetting.getCoinId(), member3.getId());
                                BigDecimal amount2 = mulRound(order.getNumber(), getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two")));
                                memberWallet2.setBalance(add(memberWallet2.getBalance(), amount2));
                                RewardRecord rewardRecord2 = new RewardRecord();
                                rewardRecord2.setAmount(amount2);
                                rewardRecord2.setCoinId(rewardPromotionSetting.getCoinId());
                                rewardRecord2.setMemberId(member3.getId());
                                rewardRecord2.setRemark(rewardPromotionSetting.getType().getDescription());
                                rewardRecord2.setType(RewardRecordType.PROMOTION);
                                rewardRecordService.save(rewardRecord2);
                            }
                        }
                    }
            );
        }
    }
}
