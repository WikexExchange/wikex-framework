package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.util.MessageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface MemberContractWalletService extends IService<MemberContractWallet> {

    MemberContractWallet findByMemberId(Long memberId);

    void modifyUsdtBuyLeverage(Long walletId, BigDecimal leverage);

    void modifyUsdtSellLeverage(Long walletId, BigDecimal leverage);

    void decreaseUsdtFrozen(Long walletId, BigDecimal amount);

    MessageResult freezeUsdtBalance(MemberContractWallet memberWallet, BigDecimal amount);

    void increaseUsdtBuyPrincipalAmountWithFrozen(Long walletId, BigDecimal amount);

    void increaseUsdtSellPrincipalAmountWithFrozen(Long walletId, BigDecimal amount);

    void updateUsdtBuyPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume);

    void updateUsdtSellPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume);

    void updateShareNumber(Long walletId, BigDecimal shareNumber);

    void decreaseUsdtFrozenSellPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount);

    void increaseUsdtBalance(Long walletId, BigDecimal amount);

    void increaseUsdtProfit(Long id, BigDecimal pL);

    void increaseUsdtLoss(Long id, BigDecimal pL);

    void decreaseUsdtFrozenBuyPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount);

    void freezeUsdtSellPosition(Long id, BigDecimal volume);

    void freezeUsdtBuyPosition(Long id, BigDecimal volume);

    List<MemberContractWallet> findAllNeedSync(ContractCoin contractCoin);

    void blastBuy(Long walletId);

    void blastSell(Long walletId);

    void decreaseUsdtBalance(Long walletId, BigDecimal amount);

    MessageResult thawUsdtBalance(MemberContractWallet memberWallet, BigDecimal amount);

    void thrawUsdtSellPosition(Long walletId, BigDecimal volume);

    void thrawUsdtBuyPosition(Long walletId, BigDecimal volume);

    void increaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount);

    void modifyUsdtBuyAndSellLeverage(Long walletId, BigDecimal leverage);

    void increaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount);

    void decreaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount);

    void decreaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount);

    List<MemberContractWallet> findAllByMemberId(Long id);

    Page<MemberContractWallet> findAll(MemberContractWalletScreen screen);

    public List<MemberContractWallet> getWalletsByBuyPosition();

    public List<MemberContractWallet> getWalletsBySellPosition();

    
    public void justDecreaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount);

    
    public void justIncreaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount) ;

    
    public void justDecreaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount) ;

    
    public void justIncreaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount);

    public void decreaseUsdtBuyPositionAndUsdtFrozenBuyPositionAndPrincipalAmount(Long id, BigDecimal position, BigDecimal frozenPosition, BigDecimal principalAmount);

    public void decreaseUsdtSellPositionAndUsdtFrozenSellPositionAndPrincipalAmount(Long id, BigDecimal position, BigDecimal frozenPosition, BigDecimal principalAmount);

    
    List<MemberContractWallet> holdingWalletList(Long contractId);

    public BigDecimal usdtTotalProfitAndLoss(Long memberId,List<ContractCoin> coins);

    public Map<String,BigDecimal> getTotalProfitAndLossAndPrincipalAmount(Long memberId, List<ContractCoin> coins,Boolean isForBlast,Map<String,List<Poke>> pokesMap);

    List<MemberContractWallet> findByNotInMemberIds(List<Long> list);
}
