package com.wikex.wikex.coinswap.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.MemberContractWalletCoinScreen;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.util.MessageResult;

import java.math.BigDecimal;
import java.util.List;


public interface MemberContractWalletCoinService extends IService<MemberContractWalletCoin> {

    MemberContractWalletCoin findByMemberIdAndContractCoin(Long memberId, ContractCoinCoin contractCoin);

    void modifyCoinBuyLeverage(Long walletId, BigDecimal leverage);

    void modifyCoinSellLeverage(Long walletId, BigDecimal leverage);

    void decreaseCoinFrozen(Long walletId, BigDecimal amount);

    MessageResult freezeCoinBalance(MemberContractWalletCoin memberWallet, BigDecimal amount);

    void increaseCoinBuyPrincipalAmountWithFrozen(Long walletId, BigDecimal amount);

    void increaseCoinSellPrincipalAmountWithFrozen(Long walletId, BigDecimal amount);

    void updateCoinBuyPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume);

    void updateCoinSellPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume);

    void updateShareNumber(Long walletId, BigDecimal shareNumber);

    void decreaseCoinFrozenSellPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount);

    void increaseCoinBalance(Long walletId, BigDecimal amount);

    void increaseCoinProfit(Long id, BigDecimal pL);

    void increaseCoinLoss(Long id, BigDecimal pL);

    void decreaseCoinFrozenBuyPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount);

    void freezeCoinSellPosition(Long id, BigDecimal volume);

    void freezeCoinBuyPosition(Long id, BigDecimal volume);

    List<MemberContractWalletCoin> findAllNeedSync(ContractCoinCoin contractCoin);

    void blastBuy(Long walletId);

    void blastSell(Long walletId);

    void decreaseCoinBalance(Long walletId, BigDecimal amount);

    MessageResult thawCoinBalance(MemberContractWalletCoin memberWallet, BigDecimal amount);

    void thrawCoinSellPosition(Long walletId, BigDecimal volume);

    void thrawCoinBuyPosition(Long walletId, BigDecimal volume);

    void increaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount);

    void modifyCoinBuyAndSellLeverage(Long walletId, BigDecimal leverage);

    void increaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount);

    void decreaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount);

    void decreaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount);

    List<MemberContractWalletCoin> findAllByMemberId(Long id);

    Page<MemberContractWalletCoin> findAll(MemberContractWalletCoinScreen screen);

    public List<MemberContractWalletCoin> getWalletsByBuyPosition(Long contractId);

    public List<MemberContractWalletCoin> getWalletsBySellPosition(Long contractId);

    
    public void justDecreaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount);

    
    public void justIncreaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount) ;

    
    public void justDecreaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount) ;

    
    public void justIncreaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount);
}
