package com.wikex.wikex.second.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Second contract trading pair
 * </p>
 *
 */
@ApiModel(value = "Second contract trading pair")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractSecondCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer baseCoinScale;

    private String baseSymbol;

    /**
     * Close position fee
     */
    @ApiModelProperty(value = "Close position fee")
    private BigDecimal closeFee;

    private Integer coinScale;

    private String coinSymbol;

    /**
     * Status
     */
    @ApiModelProperty(value = "Status")
    private Integer enable;

    /**
     * Whether market buy open position is enabled
     */
    @ApiModelProperty(value = "Whether market buy open position is enabled")
    private Integer enableMarketBuy;

    /**
     * Whether market sell open position is enabled
     */
    @ApiModelProperty(value = "Whether market sell open position is enabled")
    private Integer enableMarketSell;

    /**
     * Whether open buy position is allowed
     */
    @ApiModelProperty(value = "Whether open buy position is allowed")
    private Integer enableOpenBuy;

    /**
     * Whether open sell position is allowed
     */
    @ApiModelProperty(value = "Whether open sell position is allowed")
    private Integer enableOpenSell;

    /**
     * Whether trigger entrust for opening position is enabled
     */
    @ApiModelProperty(value = "Whether trigger entrust for opening position is enabled")
    private Integer enableTriggerEntrust;

    /**
     * Whether it is tradable
     */
    @ApiModelProperty(value = "Whether it is tradable")
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
     * Maker fee (fee for closing long position)
     */
    @ApiModelProperty(value = "Maker fee (fee for closing long position)")
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
     * Open position fee
     */
    @ApiModelProperty(value = "Open position fee")
    private BigDecimal openFee;

    /**
     * Unit lot size
     */
    @ApiModelProperty(value = "Unit lot size")
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
     * Taker fee (fee for closing short position)
     */
    @ApiModelProperty(value = "Taker fee (fee for closing short position)")
    private BigDecimal takerFee;

    /**
     * Total close position fee for contract
     */
    @ApiModelProperty(value = "Total close position fee for contract")
    private BigDecimal totalCloseFee;

    /**
     * Contract platform loss
     */
    @ApiModelProperty(value = "Contract platform loss")
    private BigDecimal totalLoss;

    /**
     * Total open position fee for contract
     */
    @ApiModelProperty(value = "Total open position fee for contract")
    private BigDecimal totalOpenFee;

    /**
     * Contract platform profit
     */
    @ApiModelProperty(value = "Contract platform profit")
    private BigDecimal totalProfit;

    /**
     * Contract type
     */
    @ApiModelProperty(value = "Contract type")
    private Integer type;

    /**
     * Frontend visible status
     */
    @ApiModelProperty(value = "Frontend visible status")
    private Integer visible;

    @TableField(exist = false)
    private Long currentTime;

}
