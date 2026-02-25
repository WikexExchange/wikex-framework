package com.wikex.wikex.swap.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


public interface MemberContractWalletMapper extends BaseMapper<MemberContractWallet> {

    void modifyUsdtBuyLeverage(@Param("walletId") Long walletId, @Param("leverage") BigDecimal leverage);

    void modifyUsdtSellLeverage(@Param("walletId") Long walletId,@Param("leverage") BigDecimal leverage);

    int decreaseUsdtFrozen(@Param("walletId") Long walletId,@Param("amount")  BigDecimal amount);

    int thawUsdtBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int freezeUsdtBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseUsdtBuyPrincipalAmountWithFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseUsdtSellPrincipalAmountWithFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void updateUsdtBuyPriceAndPosition(@Param("walletId") Long walletId, @Param("avaPrice") BigDecimal avaPrice, @Param("volume") BigDecimal volume);

    void updateUsdtSellPriceAndPosition(@Param("walletId") Long walletId, @Param("avaPrice") BigDecimal avaPrice, @Param("volume") BigDecimal volume);

    void updateShareNumber(@Param("walletId") Long walletId, @Param("shareNumber") BigDecimal shareNumber);

    void decreaseUsdtFrozenSellPositionAndPrincipalAmount(@Param("walletId") Long walletId, @Param("volume") BigDecimal volume, @Param("pAmount") BigDecimal pAmount);

    void increaseUsdtBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseUsdtProfit(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseUsdtLoss(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void decreaseUsdtFrozenBuyPositionAndPrincipalAmount(@Param("walletId") Long walletId, @Param("volume") BigDecimal volume, @Param("pAmount") BigDecimal pAmount);

    void freezeUsdtSellPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void freezeUsdtBuyPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    List<MemberContractWallet> findAllNeedSync(@Param("contractId") Long contractId);

    void blastBuy(@Param("walletId") Long walletId);

    void blastSell(@Param("walletId") Long walletId);

    void decreaseUsdtBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void thrawUsdtSellPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void thrawUsdtBuyPosition(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void increaseUsdtBuyPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void modifyUsdtBuyAndSellLeverage(@Param("walletId") Long walletId, @Param("leverage") BigDecimal leverage);

    void increaseUsdtSellPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void decreaseUsdtBuyPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    void decreaseUsdtSellPrincipalAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    List<MemberContractWallet> getWalletsByBuyPosition();

    void justDecreaseUsdtBuyPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

    List<MemberContractWallet> getWalletsBySellPosition();

    void justIncreaseUsdtSellPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

    void justDecreaseUsdtSellPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

    void justIncreaseUsdtBuyPrincipalAmount(@Param("walletId")Long walletId, @Param("amount")BigDecimal amount);

    void decreaseUsdtBuyPositionAndUsdtFrozenBuyPositionAndPrincipalAmount(@Param("walletId")Long id, @Param("position")BigDecimal position,
                                                                           @Param("frozenPosition")BigDecimal frozenPosition,@Param("principalAmount") BigDecimal principalAmount);

    void decreaseUsdtSellPositionAndUsdtFrozenSellPositionAndPrincipalAmount(@Param("walletId")Long id, @Param("position")BigDecimal position,
                                                                             @Param("frozenPosition")BigDecimal frozenPosition,@Param("principalAmount") BigDecimal principalAmount);

    List<MemberContractWallet> holdingWalletList(@Param("contractId") Long contractId);
}
