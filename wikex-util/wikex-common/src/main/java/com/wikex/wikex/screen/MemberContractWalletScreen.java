package com.wikex.wikex.screen;

import com.wikex.wikex.constant.ContractOrderPattern;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberContractWalletScreen extends PageParam{
    private Long contractId;
    private Long memberId;
    private String phone;
    private String email;
    private BigDecimal usdtBalance;
    private BigDecimal usdtFrozenBalance;
    private ContractOrderPattern usdtPattern;
    private BigDecimal usdtBuyLeverage;
    private BigDecimal usdtSellLeverage;
    private BigDecimal usdtBuyPosition;
    private BigDecimal usdtFrozenBuyPosition;
    private BigDecimal usdtBuyPrincipalAmount;
    private BigDecimal usdtSellPosition;
    private BigDecimal usdtFrozenSellPosition;
    private BigDecimal usdtSellPrincipalAmount;
    private List<Sort.Direction> direction;
    private List<String> property;;
}
