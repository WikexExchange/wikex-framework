package com.wikex.wikex.coinswap.mapper;

import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


public interface MemberContractWalletCoinMapper extends BaseMapper<MemberContractWalletCoin> {

    void modifyCoinBuyLeverage(@Param("walletId") Long walletId, @Param("leverage") BigDecimal leverage);

    void modifyCoinSellLeverage(@Param("walletId") Long walletId,@Param("leverage") BigDecimal leverage);

    void decreaseCoinFrozen(@Param("walletId") Long walletId,@Param("amount")  BigDecimal amount);

    int thawCoinBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int freezeCoinBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseCoinBuyPrincipalAmountWithFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseCoinSellPrincipalAmountWithFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void updateCoinBuyPriceAndPosition(@Param("walletId") Long walletId, @Param("avaPrice") BigDecimal avaPrice, @Param("volume") BigDecimal volume);

    void updateCoinSellPriceAndPosition(@Param("walletId") Long walletId, @Param("avaPrice") BigDecimal avaPrice, @Param("volume") BigDecimal volume);

    void updateShareNumber(@Param("walletId") Long walletId, @Param("shareNumber") BigDecimal shareNumber);

    void decreaseCoinFrozenSellPositionAndPrincipalAmount(@Param("walletId") Long walletId, @Param("volume") BigDecimal volume, @Param("pAmount") BigDecimal pAmount);

    void increaseCoinBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseCoinProfit(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseCoinLoss(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void decreaseCoinFrozenBuyPositionAndPrincipalAmount(@Param("walletId") Long walletId, @Param("volume") BigDecimal volume, @Param("pAmount") BigDecimal pAmount);

    void freezeCoinSellPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void freezeCoinBuyPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    List<MemberContractWalletCoin> findAllNeedSync(@Param("contractId") Long contractId);

    void blastBuy(@Param("walletId") Long walletId);

    void blastSell(@Param("walletId") Long walletId);

    void decreaseCoinBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void thrawCoinSellPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void thrawCoinBuyPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseCoinBuyPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void modifyCoinBuyAndSellLeverage(@Param("walletId") Long walletId, @Param("leverage") BigDecimal leverage);

    void increaseCoinSellPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void decreaseCoinBuyPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void decreaseCoinSellPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    List<MemberContractWalletCoin> getWalletsByBuyPosition(@Param("contractId") Long contractId);

    void justDecreaseCoinBuyPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

    List<MemberContractWalletCoin> getWalletsBySellPosition(@Param("contractId") Long contractId);

    void justIncreaseCoinSellPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

    void justDecreaseCoinSellPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

    void justIncreaseCoinBuyPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

}
