package com.wikex.wikex.swap.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Perpetual Contract Trading Pair
 * </p>
 *
 * @author sulinxin
 * @since 2021-08-23
 */
@ApiModel(value = "Perpetual Contract Trading Pair")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Base coin precision
     */
    @ApiModelProperty(value = "Base coin precision")
    private Integer baseCoinScale;

    /**
     * Base (settlement) coin symbol, e.g., USDT
     */
    @ApiModelProperty(value = "Base (settlement) coin symbol, e.g., USDT")
    private String baseSymbol;

    /**
     * Close position fee
     */
    @ApiModelProperty(value = "Close position fee")
    private BigDecimal closeFee;

    /**
     * Trading coin precision
     */
    @ApiModelProperty(value = "Trading coin precision")
    private Integer coinScale;

    /**
     * Trading coin symbol
     */
    @ApiModelProperty(value = "Trading coin symbol")
    private String coinSymbol;

    /**
     * Status: 0 - Disabled, 1 - Enabled
     */
    @ApiModelProperty(value = "Status: 0 - Disabled, 1 - Enabled")
    private Integer enable;

    /**
     * Enable market buy opening
     */
    @ApiModelProperty(value = "Enable market buy opening")
    private BooleanEnum enableMarketBuy;

    /**
     * Enable market sell opening
     */
    @ApiModelProperty(value = "Enable market sell opening")
    private BooleanEnum enableMarketSell;

    /**
     * Allow open buy positions
     */
    @ApiModelProperty(value = "Allow open buy positions")
    private BooleanEnum enableOpenBuy;

    /**
     * Allow open sell positions
     */
    @ApiModelProperty(value = "Allow open sell positions")
    private BooleanEnum enableOpenSell;

    /**
     * Enable trigger entrust for open positions
     */
    @ApiModelProperty(value = "Enable trigger entrust for open positions")
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
     * Interval hour (spread type)
     */
    @ApiModelProperty(value = "Interval hour (spread type)")
    private Integer intervalHour;

    /**
     * Leverage
     */
    @ApiModelProperty(value = "Leverage")
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
     * Maker fee (fee for closing long positions)
     */
    @ApiModelProperty(value = "Maker fee (fee for closing long positions)")
    private BigDecimal makerFee;

    /**
     * Maximum number of shares (lots)
     */
    @ApiModelProperty(value = "Maximum number of shares (lots)")
    private BigDecimal maxShare;

    /**
     * Minimum number of shares (lots)
     */
    @ApiModelProperty(value = "Minimum number of shares (lots)")
    private BigDecimal minShare;

    /**
     * Display name for trading coin / settlement coin
     */
    @ApiModelProperty(value = "Display name for trading coin / settlement coin")
    private String name;

    /**
     * Open position fee
     */
    @ApiModelProperty(value = "Open position fee")
    private BigDecimal openFee;

    /**
     * Unit share size
     */
    @ApiModelProperty(value = "Unit share size")
    private BigDecimal shareNumber;

    /**
     * Sort order
     */
    @ApiModelProperty(value = "Sort order")
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

    /**
     * Trading coin / settlement coin symbol
     */
    @ApiModelProperty(value = "Trading coin / settlement coin symbol")
    private String symbol;

    /**
     * Taker fee (fee for closing short positions)
     */
    @ApiModelProperty(value = "Taker fee (fee for closing short positions)")
    private BigDecimal takerFee;

    /**
     * Total contract close fee
     */
    @ApiModelProperty(value = "Total contract close fee")
    private BigDecimal totalCloseFee;

    /**
     * Contract platform loss
     */
    @ApiModelProperty(value = "Contract platform loss")
    private BigDecimal totalLoss;

    /**
     * Total contract open fee
     */
    @ApiModelProperty(value = "Total contract open fee")
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

    /**
     * Current market timestamp from server
     */
    @ApiModelProperty(value = "Current market timestamp from server")
    @TableField(exist = false)
    private Long currentTime;

    /**
     * Current price
     */
    @ApiModelProperty(value = "Current price")
    @TableField(exist = false)
    private BigDecimal currentPrice;

    /**
     * USDT price exchange rate
     */
    @ApiModelProperty(value = "USDT price exchange rate")
    @TableField(exist = false)
    private BigDecimal usdtRate = BigDecimal.valueOf(6.42);

    /**
     * Remaining settlement time (seconds)
     */
    @ApiModelProperty(value = "Remaining settlement time (seconds)")
    @TableField(exist = false)
    private Long leftTime;

    /**
     * Is Ifind market data (0 = No, 1 = Yes)
     */
    @ApiModelProperty(value = "Is Ifind market data (0 = No, 1 = Yes)")
    private int isIfind = 0;

    /**
     * Ifind market code
     */
    @ApiModelProperty(value = "Ifind market code")
    private String ifindCode;

    /**
     * Whether exchange rate conversion is needed (0 = No, 1 = Yes)
     */
    @ApiModelProperty(value = "Whether exchange rate conversion is needed (0 = No, 1 = Yes)")
    private int needConvert = 0;
}
