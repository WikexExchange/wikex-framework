package com.wikex.wikex.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.screen.MemberApplicationScreen;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.mapper.MemberApplicationMapper;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.vo.MemberApplicationVo;
import com.wikex.wikex.util.BigDecimalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Service
public class MemberApplicationServiceImpl extends ServiceImpl<MemberApplicationMapper, MemberApplication> implements MemberApplicationService {

    @Autowired
    private MemberService memberService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private MemberPromotionService memberPromotionService;

    @Value("${commission.need.real-name:0}")
    private int needRealName ;

    @Value("${commission.promotion.second-level:0}")
    private int promotionSecondLevel ;

    @Override
    public List<MemberApplication> findLatelyReject(Long memberId) {
        QueryWrapper<MemberApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId).eq("audit_status", AuditStatus.AUDIT_DEFEATED.getCode());
        queryWrapper.orderByDesc("id");
        return this.list(queryWrapper);
    }

    @Override
    public int queryByIdCard(String idCard) {
        return this.baseMapper.queryByIdCard(idCard);
    }

    @Override
    public Page<MemberApplicationVo> findAll(MemberApplicationScreen screen) {
        Page<MemberApplication> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        return this.baseMapper.findAll(page,screen);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(MemberApplication application) {
        Member member = memberService.getById(application.getMemberId());
        member.setMemberLevel(MemberLevelEnum.REALNAME.getCode());
        member.setRealName(application.getRealName());
        member.setIdNumber(application.getIdCard());
        member.setRealNameStatus(RealNameStatus.VERIFIED.getCode());
        member.setApplicationTime(new Date());
        memberService.updateById(member);
        application.setAuditStatus(AuditStatus.AUDIT_SUCCESS);
        
        if(needRealName==1){
            if(member.getInviterId() != null) {
                Member member1 = memberService.getById(member.getInviterId());
                promotion(member1, member);
            }
        }
        
        
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.REGISTER);
        if (rewardPromotionSetting != null) {
            BigDecimal amount1 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one");
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(rewardPromotionSetting.getCoinId(), member.getId());
            memberWallet.setBalance(BigDecimalUtils.add(memberWallet.getBalance(), amount1));
            memberWalletService.saveOrUpdate(memberWallet);
            RewardRecord rewardRecord = new RewardRecord();
            rewardRecord.setAmount(amount1);
            rewardRecord.setCoinId(rewardPromotionSetting.getCoinId());
            rewardRecord.setMemberId(member.getId());
            rewardRecord.setRemark(rewardPromotionSetting.getType().getDescription());
            rewardRecord.setType(RewardRecordType.PROMOTION);
            rewardRecordService.save(rewardRecord);
            MemberTransaction memberTransactionMember = new MemberTransaction();
            memberTransactionMember.setFee(BigDecimal.ZERO);
            memberTransactionMember.setAmount(amount1);
            memberTransactionMember.setSymbol(rewardPromotionSetting.getCoinId());
            memberTransactionMember.setType(TransactionType.PROMOTION_AWARD.getCode());
            memberTransactionMember.setMemberId(member.getId());
            memberTransactionMember.setRealFee("0");
            memberTransactionMember.setDiscountFee("0");
            memberTransactionMember.setCreateTime(new Date());
            memberTransactionService.save(memberTransactionMember);

            
            try {
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.saveOrUpdate(application);
    }

    
    private void promotion(Member member1, Member member) {

        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.REGISTER);
        if (rewardPromotionSetting != null) {
            MemberWallet memberWallet1 = memberWalletService.findByCoinUnitAndMemberId(rewardPromotionSetting.getCoinId(), member1.getId());

            BigDecimal amount1 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one");
            memberWallet1.setBalance(BigDecimalUtils.add(memberWallet1.getBalance(), amount1));
            memberWalletService.saveOrUpdate(memberWallet1);
            RewardRecord rewardRecord1 = new RewardRecord();
            rewardRecord1.setAmount(amount1);
            rewardRecord1.setCoinId(rewardPromotionSetting.getCoinId());
            rewardRecord1.setMemberId(member1.getId());
            rewardRecord1.setRemark(rewardPromotionSetting.getType().getDescription());
            rewardRecord1.setType(RewardRecordType.PROMOTION);
            rewardRecordService.save(rewardRecord1);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(amount1);
            memberTransaction.setSymbol(rewardPromotionSetting.getCoinId());
            memberTransaction.setType(TransactionType.PROMOTION_AWARD.getCode());
            memberTransaction.setMemberId(member1.getId());
            memberTransaction.setRealFee("0");
            memberTransaction.setDiscountFee("0");
            memberTransaction.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction);

            
            try {
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        member1.setFirstLevel(member1.getFirstLevel() + 1);

        MemberPromotion one = new MemberPromotion();
        one.setInviterId(member1.getId());
        one.setInviteesId(member.getId());
        one.setLevel(0);
        memberPromotionService.save(one);
        
        if(promotionSecondLevel == 1) {
            if (member1.getInviterId() != null) {
                Member member2 = memberService.getById(member1.getInviterId());
                
                promotionLevelTwo(rewardPromotionSetting, member2, member);
            }
        }
    }

    private void promotionLevelTwo(RewardPromotionSetting rewardPromotionSetting, Member member2, Member member) {
        if (rewardPromotionSetting != null) {
            MemberWallet memberWallet2 = memberWalletService.findByCoinUnitAndMemberId(rewardPromotionSetting.getCoinId(), member2.getId());
            BigDecimal amount2 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two");
            memberWallet2.setBalance(BigDecimalUtils.add(memberWallet2.getBalance(), amount2));
            memberWalletService.saveOrUpdate(memberWallet2);
            RewardRecord rewardRecord2 = new RewardRecord();
            rewardRecord2.setAmount(amount2);
            rewardRecord2.setCoinId(rewardPromotionSetting.getCoinId());
            rewardRecord2.setMemberId(member2.getId());
            rewardRecord2.setRemark(rewardPromotionSetting.getType().getDescription());
            rewardRecord2.setType(RewardRecordType.PROMOTION);
            rewardRecordService.save(rewardRecord2);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(amount2);
            memberTransaction.setSymbol(rewardPromotionSetting.getCoinId());
            memberTransaction.setType(TransactionType.PROMOTION_AWARD.getCode());
            memberTransaction.setMemberId(member2.getId());
            memberTransaction.setRealFee("0");
            memberTransaction.setDiscountFee("0");
            memberTransaction.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction);

            
            try {
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        member2.setSecondLevel(member2.getSecondLevel() + 1);
        MemberPromotion two = new MemberPromotion();
        two.setInviterId(member2.getId());
        two.setInviteesId(member.getId());
        two.setLevel(1);
        memberPromotionService.save(two);
        if (member2.getInviterId() != null) {
            Member member3 = memberService.getById(member2.getInviterId());
            member3.setThirdLevel(member3.getThirdLevel() + 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditNotPass(MemberApplication application) {
        Member member = memberService.getById(application.getMemberId());
        member.setRealNameStatus(RealNameStatus.NOT_CERTIFIED.getCode());
        member.setMemberLevel(MemberLevelEnum.GENERAL.getCode());
        member.setRealName(null);
        member.setIdNumber(null);
        member.setApplicationTime(null);
        memberService.saveOrUpdate(member);
        application.setAuditStatus(AuditStatus.AUDIT_DEFEATED);
        this.saveOrUpdate(application);
    }

    @Override
    public Integer countAuditing() {
        LambdaQueryWrapper<MemberApplication> query = new LambdaQueryWrapper<>();
        query.eq(MemberApplication::getAuditStatus,AuditStatus.AUDIT_ING.getCode());
        return this.baseMapper.selectCount(query);
    }


}
