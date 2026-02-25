package com.wikex.wikex.coinswap.entity;

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
 * Member Perpetual Contract Wallet (Coin-margined)
 * </p>
 *
 * Author: markchao
 * Since: 2021-06-21
 */
@ApiModel(value = "Member Perpetual Contract Wallet")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberContractWalletCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Coin balance
     */
    @ApiModelProperty(value = "Coin balance")
    private BigDecimal coinBalance;

    /**
     * Long position leverage multiplier
     */
    @ApiModelProperty(value = "Long leverage multiplier")
    private BigDecimal coinBuyLeverage;

    /**
     * Long position size
     */
    @ApiModelProperty(value = "Long position size")
    private BigDecimal coinBuyPosition;

    /**
     * Average entry price for long position
     */
    @ApiModelProperty(value = "Long position average price")
    private BigDecimal coinBuyPrice;

    /**
     * Margin for long position
     */
    @ApiModelProperty(value = "Long position margin")
    private BigDecimal coinBuyPrincipalAmount;

    /**
     * Frozen balance
     */
    @ApiModelProperty(value = "Frozen balance")
    private BigDecimal coinFrozenBalance;

    /**
     * Frozen long position size
     */
    @ApiModelProperty(value = "Frozen long position size")
    private BigDecimal coinFrozenBuyPosition;

    /**
     * Frozen short position size
     */
    @ApiModelProperty(value = "Frozen short position size")
    private BigDecimal coinFrozenSellPosition;

    /**
     * Coin-margined position mode
     */
    @ApiModelProperty(value = "Coin-margined position mode")
    private ContractOrderPattern coinPattern;

    /**
     * Short position leverage multiplier
     */
    @ApiModelProperty(value = "Short leverage multiplier")
    private BigDecimal coinSellLeverage;

    /**
     * Short position size
     */
    @ApiModelProperty(value = "Short position size")
    private BigDecimal coinSellPosition;

    /**
     * Average entry price for short position
     */
    @ApiModelProperty(value = "Short position average price")
    private BigDecimal coinSellPrice;

    /**
     * Margin for short position
     */
    @ApiModelProperty(value = "Short position margin")
    private BigDecimal coinSellPrincipalAmount;

    /**
     * Number of contracts (short)
     */
    @ApiModelProperty(value = "Number of contracts (short)")
    private BigDecimal coinShareNumber;

    /**
     * Coin-margined unrealized loss
     */
    @ApiModelProperty(value = "Coin-margined unrealized loss")
    private BigDecimal coinLoss;

    /**
     * Coin-margined unrealized profit
     */
    @ApiModelProperty(value = "Coin-margined unrealized profit")
    private BigDecimal coinProfit;

    private Long memberId;

    private Long contractId;

    /**
     * Total unrealized PnL in USDT (long + short)
     */
    @TableField(exist = false)
    private BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;

    /**
     * Total unrealized PnL in coin (long + short)
     */
    @TableField(exist = false)
    private BigDecimal coinTotalProfitAndLoss = BigDecimal.ZERO;

    /**
     * Current market price
     */
    @TableField(exist = false)
    private BigDecimal currentPrice;

    /**
     * CNY exchange rate
     */
    @TableField(exist = false)
    private BigDecimal cnyRate = BigDecimal.valueOf(7L);

    /**
     * Trading pair symbol
     */
    @TableField(exist = false)
    private String symbol;

    /**
     * Associated contract coin info
     */
    @TableField(exist = false)
    private ContractCoinCoin contractCoin;

    /**
     * Forced liquidation price for long
     */
    @TableField(exist = false)
    private BigDecimal buyForcePrice = BigDecimal.valueOf(0L);

    /**
     * Forced liquidation price for short
     */
    @TableField(exist = false)
    private BigDecimal sellForcePrice = BigDecimal.valueOf(0L);
}
