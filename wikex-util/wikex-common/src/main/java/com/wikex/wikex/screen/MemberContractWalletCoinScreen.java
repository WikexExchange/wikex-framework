package com.wikex.wikex.screen;

import com.wikex.wikex.constant.ContractOrderPattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberContractWalletCoinScreen extends PageParam{
    private Long contractId;
    private Long memberId;
    private String phone;
    private String email;
    private BigDecimal coinBalance;
    private BigDecimal coinFrozenBalance;
    private ContractOrderPattern coinPattern;
    private BigDecimal coinBuyLeverage;
    private BigDecimal coinSellLeverage;
    private BigDecimal coinBuyPosition;
    private BigDecimal coinFrozenBuyPosition;
    private BigDecimal coinBuyPrincipalAmount;
    private BigDecimal coinSellPosition;
    private BigDecimal coinFrozenSellPosition;
    private BigDecimal coinSellPrincipalAmount;
}
