package com.wikex.wikex.user.service;

import com.wikex.wikex.user.entity.AgentWallet;

import java.math.BigDecimal;
import java.util.List;

public interface AgentWalletService {

     List<AgentWallet> findAll();

     AgentWallet findWalletByMemberIdAndCoinUnit(Long memberId,String coinUnit);

     void increaseBalance(Long id,BigDecimal amount);

     List<AgentWallet> findAllByMemberId(Long memberId);

     AgentWallet findOne(Long id) ;

     void decreaseBalance(Long id, BigDecimal amount) ;

     void setAgentWalletBalance(Long id, BigDecimal amount);
}


