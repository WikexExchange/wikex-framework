package com.wikex.wikex.swap.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOrderEntrustType;
import com.wikex.wikex.constant.ContractRewardRecordType;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.swap.entity.ContractRewardRecord;
import com.wikex.wikex.swap.mapper.ContractOrderEntrustMapper;
import com.wikex.wikex.swap.mapper.ContractRewardRecordMapper;
import com.wikex.wikex.swap.service.ContractRewardRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.feign.MemberWeightUpperFeign;
import com.wikex.wikex.util.BigDecimalUtils;
import com.wikex.wikex.util.MessageResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class ContractRewardRecordServiceImpl extends ServiceImpl<ContractRewardRecordMapper, ContractRewardRecord> implements ContractRewardRecordService {


    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private ContractOrderEntrustMapper contractOrderEntrustMapper;
    @Autowired
    private MemberWeightUpperFeign memberWeightUpperFeign;
    @Value("${dictionary.commissionRate}")
    private String commission;
    @Value("${dictionary.levelRewardRate}")
    private String levelRewardRate;

    private String key = "admin-all-rewardset";
    private String agentKey = "agent-all-rewardset-";

    @Autowired
    private RedisTemplate redisTemplate;


    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendReward(ContractOrderEntrust orderEntrust) {

        BigDecimal lave = sendReturnReward(orderEntrust);
        
        Member member = memberFeign.findMemberById(orderEntrust.getMemberId());

        if(member==null){
            
            return;
        }
        
        Long firstId=3L;
        Long secondId=4L;
        BigDecimal rate1=BigDecimal.valueOf(0.3);
        BigDecimal rate2=BigDecimal.valueOf(0.7);


        Member member2 = memberFeign.findMemberById(secondId);

        if(lave.compareTo(BigDecimal.ZERO)==1){
            
            MemberWallet memberWallet = memberWalletFeign.findByCoinUnitAndMemberId("USDT", firstId);
            BigDecimal reward = BigDecimalUtils.mulDown(lave,rate1, 8);
            if (reward.compareTo(BigDecimal.ZERO) > 0 && memberWallet != null) {
                memberWalletFeign.increaseBalance(memberWallet.getId(), reward);
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(reward);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setMemberId(firstId);
                memberTransaction.setSymbol("USDT");
                memberTransaction.setType(TransactionType.PLATFORM_FEE_AWARD.getCode());
                memberTransaction.setDiscountFee("0");
                memberTransaction.setRealFee("0");
                memberTransactionFeign.save(memberTransaction);
                ContractRewardRecord rewardRecord = new ContractRewardRecord();
                rewardRecord.setCoinId(memberWallet.getCoinId());
                rewardRecord.setOrderId(orderEntrust.getId());
                rewardRecord.setMemberId(member2.getId());
                rewardRecord.setFromMemberId(member.getId());
                rewardRecord.setType(ContractRewardRecordType.PLATFORM);
                rewardRecord.setNum(reward);
                this.save(rewardRecord);
            }
            
            MemberWallet memberWallet2 = memberWalletFeign.findByCoinUnitAndMemberId("USDT", secondId);
            BigDecimal reward2 = BigDecimalUtils.mulDown(lave,rate2, 8);
            if (reward2.compareTo(BigDecimal.ZERO) > 0 && memberWallet2 != null) {
                memberWalletFeign.increaseBalance(memberWallet2.getId(), reward2);
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(reward2);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setMemberId(secondId);
                memberTransaction.setSymbol("USDT");
                memberTransaction.setType(TransactionType.PLATFORM_FEE_AWARD.getCode());
                memberTransaction.setDiscountFee("0");
                memberTransaction.setRealFee("0");
                memberTransactionFeign.save(memberTransaction);
                ContractRewardRecord rewardRecord = new ContractRewardRecord();
                rewardRecord.setCoinId(memberWallet2.getCoinId());
                rewardRecord.setOrderId(orderEntrust.getId());
                rewardRecord.setMemberId(member2.getId());
                rewardRecord.setFromMemberId(member.getId());
                rewardRecord.setType(ContractRewardRecordType.PLATFORM);
                rewardRecord.setNum(reward2);
                this.save(rewardRecord);
            }
        }


    }

    @Override
    public Page<ContractRewardRecord> findAll(ContractRewardRecordScreen screen) {
        Page<ContractRewardRecord> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        QueryWrapper<ContractRewardRecord> queryWrapper = new QueryWrapper<>();
        if(screen.getStartTime() != null) {
            queryWrapper.ge("create_time",screen.getStartTime());
        }
        if(screen.getEndTime() != null) {
            queryWrapper.le("create_time",screen.getEndTime());
        }
        if(screen.getMemberId() != null) {
            queryWrapper.eq("member_id",screen.getMemberId());
        }
        if(screen.getType() != null) {
            queryWrapper.eq("type",screen.getType());
        }
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }

    @Override
    public RewardSetVo findAllRewardSetVo() {
        RewardSetVo vo = null;
        ValueOperations<String,String> opt = redisTemplate.opsForValue();
        String voJson = opt.get(key);
        if(voJson!=null){
            vo = JSONObject.parseObject(voJson,RewardSetVo.class);
            return vo;
        }
        vo = new RewardSetVo();
        vo.setName("Super Admin");
        vo.setRate("");
        List<Member> members = memberFeign.findAllList();
        List<RewardSetVo> children = new ArrayList<>();
        for(Member member:members){
            children.add(findRewardSetVoByPid(member.getId(),member.getId(),false));
        }
        vo.setChildren(children);
        
        String jsonString = JSONObject.toJSONString(vo);
        opt.set(key,jsonString);
        return vo;
    }

    @Override
    public void clearAllRewardSetVo() {
        ValueOperations<String,String> opt = redisTemplate.opsForValue();
        opt.set(key,null);
    }

    @Override
    public void clearRewardSetVoById(Long memberId) {
        String key = agentKey+memberId;
        ValueOperations<String,String> opt = redisTemplate.opsForValue();
        opt.set(key,null);
    }

    private RewardSetVo findRewardSetVoByPid(Long userId,Long pid,Boolean canUpdate){
        RewardSetVo vo = new RewardSetVo();
        vo.setCanUpdate(canUpdate);
        Member member = memberFeign.findMemberById(pid);
        if("1".equals(member.getSuperPartner())){
            vo.setRate("100%");
            vo.setCanUpdate(false);
            if(userId.longValue()!=pid){
                canUpdate=false;
            }
        }else {
            MemberWeightUpper upper = memberWeightUpperFeign.saveMemberWeightUpper(member);
            vo.setRate(upper.getRate()+"%");
        }
        vo.setName(member.getUsername());
        vo.setRealName((member.getRealName()!=null || "".equals(member.getRealName()))?member.getRealName():"Not verified");
        vo.setId(member.getId()+"");
        List<RewardSetVo> children = new ArrayList<>();
        List<Member> members = memberFeign.findPromotionMember(pid);
        for(Member member1:members){
            children.add(this.findRewardSetVoByPid(userId,member1.getId(),canUpdate));
        }
        vo.setChildren(children);
        return vo;
    }

    
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal sendReturnReward(ContractOrderEntrust orderEntrust) {
        BigDecimal lave = BigDecimal.ZERO;
        if(orderEntrust==null){
            return lave;
        }
        BigDecimal fee =BigDecimal.ZERO;
        ContractRewardRecordType type = ContractRewardRecordType.OPEN;
        if(orderEntrust.getEntrustType()== ContractOrderEntrustType.OPEN){
            fee = orderEntrust.getOpenFee();
        }else {
            fee = orderEntrust.getCloseFee();
            type = ContractRewardRecordType.CLOSE;
        }
        lave = fee;
        
        contractOrderEntrustMapper.updateReward(orderEntrust.getId(),1);
        
        MemberWeightUpper upper = memberWeightUpperFeign.findMemberWeightUpperByMemberId(orderEntrust.getMemberId());
        if(upper==null || upper.getFirstMemberId()==null){
            
            return lave;
        }
        
        Member member = memberFeign.findMemberById(orderEntrust.getMemberId());
        if(member==null){
            
            return lave;
        }
        if(StringUtils.isEmpty(upper.getUpper())){
            
            return lave;
        }
        MessageResult<List<Member>> superPartnerMembersMessageResult = memberFeign.findSuperPartnerMembersByIds(upper.getUpper());
        List<Member> supers = null;
        if(superPartnerMembersMessageResult!=null){
            supers = superPartnerMembersMessageResult.getData();
        }
        if(supers==null || supers.size()==0){
            
            return lave;
        }
        
        List<MemberWeightUpper> uppers = memberWeightUpperFeign.findAllByUpperIds(upper.getUpper());
        if(uppers==null || uppers.size()==0){
            
            return lave;
        }

        BigDecimal totalReward = BigDecimal.ZERO;
        if(commission==null){
            
            totalReward = BigDecimalUtils.mulRound(fee,BigDecimal.valueOf(0.5), 8);
        }else {
            totalReward = BigDecimalUtils.mulRound(fee,BigDecimal.valueOf(Double.parseDouble(commission)), 8);
        }
        
        int currentRate = 0;
        
        if(upper.getRate()>0){
            currentRate=upper.getRate();
            
            BigDecimal rate = BigDecimal.valueOf(upper.getRate()).divide(BigDecimal.valueOf(100),8,BigDecimal.ROUND_DOWN);
            
            MemberWallet memberWallet = memberWalletFeign.findByCoinUnitAndMemberId("USDT", upper.getMemberId());
            BigDecimal reward = BigDecimalUtils.mulDown(totalReward,rate, 8);
            if (reward.compareTo(BigDecimal.ZERO) > 0 && memberWallet != null) {
                memberWalletFeign.increaseBalance(memberWallet.getId(), reward);
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(reward);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setMemberId(upper.getMemberId());
                memberTransaction.setSymbol("USDT");
                memberTransaction.setType(TransactionType.CONTRACT_AWARD.getCode());
                memberTransaction.setDiscountFee("0");
                memberTransaction.setRealFee("0");
                memberTransactionFeign.save(memberTransaction);
                ContractRewardRecord rewardRecord = new ContractRewardRecord();
                rewardRecord.setCoinId(memberWallet.getCoinId());
                rewardRecord.setOrderId(orderEntrust.getId());
                rewardRecord.setMemberId(member.getId());
                rewardRecord.setFromMemberId(member.getId());
                rewardRecord.setType(type);
                rewardRecord.setNum(reward);
                this.save(rewardRecord);
            }
        }
        for(MemberWeightUpper weightUpper : uppers){
            
            Member upMember = memberFeign.findMemberById(weightUpper.getMemberId());
            int userRate = weightUpper.getRate();
            if("1".equals(upMember.getSuperPartner())){
                userRate=100;
            }
            
            int releaseRate = userRate-currentRate;
            if(releaseRate<=0){
                
                continue;
            }
            currentRate=userRate;
            BigDecimal rate = BigDecimal.valueOf(releaseRate).divide(BigDecimal.valueOf(100),8,BigDecimal.ROUND_DOWN);
            
            MemberWallet memberWallet = memberWalletFeign.findByCoinUnitAndMemberId("USDT", weightUpper.getMemberId());
            BigDecimal reward = BigDecimalUtils.mulDown(totalReward,rate, 8);
            if (reward.compareTo(BigDecimal.ZERO) > 0 && memberWallet != null) {
                memberWalletFeign.increaseBalance(memberWallet.getId(), reward);
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(reward);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setMemberId(weightUpper.getMemberId());
                memberTransaction.setSymbol("USDT");
                memberTransaction.setType(TransactionType.CONTRACT_AWARD.getCode());
                memberTransaction.setDiscountFee("0");
                memberTransaction.setRealFee("0");
                memberTransactionFeign.save(memberTransaction);
                ContractRewardRecord rewardRecord = new ContractRewardRecord();
                rewardRecord.setCoinId(memberWallet.getCoinId());
                rewardRecord.setOrderId(orderEntrust.getId());
                rewardRecord.setMemberId(upMember.getId());
                rewardRecord.setFromMemberId(member.getId());
                rewardRecord.setType(type);
                rewardRecord.setNum(reward);
                this.save(rewardRecord);
            }
            if(currentRate>=100){
                
                break;
            }
        }
        lave = lave.subtract(totalReward);


        
        if(supers.size()>1){
            BigDecimal rate = BigDecimal.ZERO;
            if(levelRewardRate==null){
                
                rate = BigDecimal.valueOf(0.02);
            }else {
                rate = BigDecimal.valueOf(Double.parseDouble(levelRewardRate));
            }

            
            MemberWallet memberWallet = memberWalletFeign.findByCoinUnitAndMemberId("USDT", supers.get(1).getId());
            BigDecimal reward = BigDecimalUtils.mulDown(totalReward,rate, 8);
            if (reward.compareTo(BigDecimal.ZERO) > 0 && memberWallet != null) {
                memberWalletFeign.increaseBalance(memberWallet.getId(), reward);
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(reward);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setMemberId(supers.get(1).getId());
                memberTransaction.setSymbol("USDT");
                memberTransaction.setType(TransactionType.LEVEL_AWARD.getCode());
                memberTransaction.setDiscountFee("0");
                memberTransaction.setRealFee("0");
                memberTransactionFeign.save(memberTransaction);
                ContractRewardRecord rewardRecord = new ContractRewardRecord();
                rewardRecord.setCoinId(memberWallet.getCoinId());
                rewardRecord.setOrderId(orderEntrust.getId());
                rewardRecord.setMemberId(supers.get(1).getId());
                rewardRecord.setFromMemberId(member.getId());
                rewardRecord.setType(ContractRewardRecordType.LEVEL);
                rewardRecord.setNum(reward);
                this.save(rewardRecord);
                lave = lave.subtract(reward);
            }
        }
        return lave;
    }
}
