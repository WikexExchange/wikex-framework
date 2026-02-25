package com.wikex.wikex.swap.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.ContractOrderPattern;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Member Perpetual Contract Wallet
 * </p>
 * 
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Perpetual Contract Wallet")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberContractWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "member id")
    private Long memberId;

    /**
     * USDT available balance
     */
    @ApiModelProperty(value = "USDT balance")
    private BigDecimal usdtBalance;

    /**
     * Long leverage multiplier
     */
    @ApiModelProperty(value = "long leverage multiplier")
    private BigDecimal usdtBuyLeverage;

    /**
     * Long position size
     */
    @ApiModelProperty(value = "long position size")
    private BigDecimal usdtBuyPosition;

    /**
     * Average long price
     */
    @ApiModelProperty(value = "average long price")
    private BigDecimal usdtBuyPrice;

    /**
     * Long margin
     */
    @ApiModelProperty(value = "long margin")
    private BigDecimal usdtBuyPrincipalAmount;

    /**
     * Frozen balance
     */
    @ApiModelProperty(value = "frozen balance")
    private BigDecimal usdtFrozenBalance;

    /**
     * Frozen long position size
     */
    @ApiModelProperty(value = "frozen long position size")
    private BigDecimal usdtFrozenBuyPosition;

    /**
     * Frozen short position size
     */
    @ApiModelProperty(value = "frozen short position size")
    private BigDecimal usdtFrozenSellPosition;

    /**
     * USDT loss
     */
    @ApiModelProperty(value = "USDT loss")
    private BigDecimal usdtLoss;

    /**
     * Coin-margined position mode
     */
    @ApiModelProperty(value = "coin-margined position mode")
    private ContractOrderPattern usdtPattern;

    /**
     * USDT profit
     */
    @ApiModelProperty(value = "USDT profit")
    private BigDecimal usdtProfit;

    /**
     * Short leverage multiplier
     */
    @ApiModelProperty(value = "short leverage multiplier")
    private BigDecimal usdtSellLeverage;

    /**
     * Short position size
     */
    @ApiModelProperty(value = "short position size")
    private BigDecimal usdtSellPosition;

    /**
     * Average short price
     */
    @ApiModelProperty(value = "average short price")
    private BigDecimal usdtSellPrice;

    /**
     * Short margin
     */
    @ApiModelProperty(value = "short margin")
    private BigDecimal usdtSellPrincipalAmount;

    /**
     * Short position size (share number)
     */
    @ApiModelProperty(value = "short position size (share number)")
    private BigDecimal usdtShareNumber;

    @TableField(exist = false)
    private BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO; // Total contract equity (long + short)

    @TableField(exist = false)
    private BigDecimal coinTotalProfitAndLoss = BigDecimal.ZERO; // Total contract equity (long + short)

    @TableField(exist = false)
    private BigDecimal currentPrice;

    @TableField(exist = false)
    private BigDecimal cnyRate = BigDecimal.valueOf(7L);

    @TableField(exist = false)
    private String symbol;

    @TableField(exist = false)
    private BigDecimal buyForcePrice = BigDecimal.valueOf(0L);
    @TableField(exist = false)
    private BigDecimal sellForcePrice = BigDecimal.valueOf(0L);

    @TableField(exist = false)
    private ContractCoin contractCoin;

    @TableField(exist = false)
    private BigDecimal userQY = BigDecimal.valueOf(0L);
    @TableField(exist = false)
    private BigDecimal canUse = BigDecimal.valueOf(0L);
    @TableField(exist = false)
    private String riskRate = "0.00%";
}
