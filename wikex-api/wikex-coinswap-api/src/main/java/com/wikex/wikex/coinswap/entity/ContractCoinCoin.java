package com.wikex.wikex.coinswap.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Perpetual Contract Trading Pair
 * </p>
 *
 * Author: sulinxin
 * Since: 2021-08-23
 */
@ApiModel(value = "Perpetual Contract Trading Pair")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractCoinCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Base coin decimal precision")
    private Integer baseCoinScale;

    @ApiModelProperty(value = "Base coin / Settlement coin")
    private String baseSymbol;

    /**
     * Closing fee
     */
    @ApiModelProperty(value = "Closing fee")
    private BigDecimal closeFee;

    @ApiModelProperty(value = "Trading coin decimal precision")
    private Integer coinScale;

    @ApiModelProperty(value = "Currency symbol, e.g., BTC")
    private String coinSymbol;

    /**
     * Status
     */
    @ApiModelProperty(value = "Status")
    private Integer enable;

    /**
     * Enable market order to open long position
     */
    @ApiModelProperty(value = "Enable market order to open long position")
    private BooleanEnum enableMarketBuy;

    /**
     * Enable market order to open short position
     */
    @ApiModelProperty(value = "Enable market order to open short position")
    private BooleanEnum enableMarketSell;

    /**
     * Allow opening long position
     */
    @ApiModelProperty(value = "Allow opening long position")
    private BooleanEnum enableOpenBuy;

    /**
     * Allow opening short position
     */
    @ApiModelProperty(value = "Allow opening short position")
    private BooleanEnum enableOpenSell;

    /**
     * Enable planned order for opening position
     */
    @ApiModelProperty(value = "Enable planned order for opening position")
    private BooleanEnum enableTriggerEntrust;

    /**
     * Is tradable
     */
    @ApiModelProperty(value = "Is tradable")
    private Integer exchangeable;

    /**
     * Overnight fee rate
     */
    @ApiModelProperty(value = "Overnight fee rate")
    private BigDecimal feePercent;

    /**
     * Spread type
     */
    @ApiModelProperty(value = "Spread type")
    private Integer intervalHour;

    private String leverage;

    /**
     * Leverage type
     */
    @ApiModelProperty(value = "Leverage type")
    private Integer leverageType;

    /**
     * Maintenance margin rate
     */
    @ApiModelProperty(value = "Maintenance margin rate")
    private BigDecimal maintenanceMarginRate;

    /**
     * Close long fee
     */
    @ApiModelProperty(value = "Close long fee")
    private BigDecimal makerFee;

    /**
     * Maximum lot size
     */
    @ApiModelProperty(value = "Maximum lot size")
    private BigDecimal maxShare;

    /**
     * Minimum lot size
     */
    @ApiModelProperty(value = "Minimum lot size")
    private BigDecimal minShare;

    private String name;

    /**
     * Opening fee
     */
    @ApiModelProperty(value = "Opening fee")
    private BigDecimal openFee;

    /**
     * Units per lot
     */
    @ApiModelProperty(value = "Units per lot")
    private BigDecimal shareNumber;

    private Integer sort;

    /**
     * Spread
     */
    @ApiModelProperty(value = "Spread")
    private BigDecimal spread;

    /**
     * Spread type
     */
    @ApiModelProperty(value = "Spread type")
    private Integer spreadType;

    private String symbol;

    /**
     * Close short fee
     */
    @ApiModelProperty(value = "Close short fee")
    private BigDecimal takerFee;

    /**
     * Total closing fee for the contract
     */
    @ApiModelProperty(value = "Total closing fee for the contract")
    private BigDecimal totalCloseFee;

    /**
     * Platform loss from contract
     */
    @ApiModelProperty(value = "Platform loss from contract")
    private BigDecimal totalLoss;

    /**
     * Total opening fee for the contract
     */
    @ApiModelProperty(value = "Total opening fee for the contract")
    private BigDecimal totalOpenFee;

    /**
     * Platform profit from contract
     */
    @ApiModelProperty(value = "Platform profit from contract")
    private BigDecimal totalProfit;

    /**
     * Contract type
     */
    @ApiModelProperty(value = "Contract type")
    private Integer type;

    /**
     * Front-end visibility status
     */
    @ApiModelProperty(value = "Front-end visibility status")
    private Integer visible;

    /**
     * Current server market price timestamp
     */
    @ApiModelProperty(value = "Current server market price timestamp")
    @TableField(exist = false)
    private Long currentTime;

    /**
     * Current price
     */
    @ApiModelProperty(value = "Current price")
    @TableField(exist = false)
    private BigDecimal currentPrice;

    /**
     * USDT exchange rate
     */
    @ApiModelProperty(value = "USDT exchange rate")
    @TableField(exist = false)
    private BigDecimal usdtRate = BigDecimal.valueOf(6.42);

    /**
     * Remaining settlement time (seconds)
     */
    @ApiModelProperty(value = "Remaining settlement time (seconds)")
    @TableField(exist = false)
    private Long leftTime;
}
