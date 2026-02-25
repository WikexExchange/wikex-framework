package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.AgentWallet;
import com.wikex.wikex.user.mapper.AgentWalletMapper;
import com.wikex.wikex.user.service.AgentWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class AgentWalletServiceImpl extends ServiceImpl<AgentWalletMapper, AgentWallet> implements AgentWalletService {



    @Override
    public List<AgentWallet> findAll() {
        return this.list();
    }

    @Override
    public AgentWallet findWalletByMemberIdAndCoinUnit(Long memberId,String coinUnit) {
        LambdaQueryWrapper<AgentWallet> query = new LambdaQueryWrapper<>();
        query.eq(AgentWallet::getMemberId,memberId);
        query.eq(AgentWallet::getCoinUnit,coinUnit);
        AgentWallet wallet = this.getOne(query);
        if(wallet==null){
            
            wallet = new AgentWallet();
            wallet.setMemberId(memberId);
            wallet.setCoinUnit(coinUnit);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setCreateTime(System.currentTimeMillis());
            wallet.setUpdateTime(System.currentTimeMillis());
            this.save(wallet);
        }
        return  wallet;
    }
    @Override
    public void increaseBalance(Long id,BigDecimal amount) {
        this.baseMapper.increaseBalance(id,amount);
    }
    @Override
    public List<AgentWallet> findAllByMemberId(Long memberId) {
        LambdaQueryWrapper<AgentWallet> query = new LambdaQueryWrapper<>();
        query.eq(AgentWallet::getMemberId,memberId);
        return this.list(query);
    }

    @Override
    public AgentWallet findOne(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public void decreaseBalance(Long id, BigDecimal amount) {
        this.baseMapper.decreaseBalance(id,amount);
    }

    @Override
    public void setAgentWalletBalance(Long id, BigDecimal amount) {
        this.baseMapper.setAgentWalletBalance(id,amount);
    }
}


