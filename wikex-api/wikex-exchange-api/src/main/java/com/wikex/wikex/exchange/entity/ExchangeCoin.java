package com.wikex.wikex.exchange.entity;

import java.math.BigDecimal;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.ExchangeCoinPublishType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Transient;

/**
 * <p>
 * Spot trading pair
 * </p>
 */
@ApiModel(value = "Spot trading pair")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExchangeCoin implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId("symbol")
    private String symbol;

    private Integer baseCoinScale;

    private String baseSymbol;

    private Integer coinScale;

    private String coinSymbol;

    private Integer enable;

    /**
     * Trading fee
     */
    @ApiModelProperty(value = "Trading fee")
    private BigDecimal fee;

    private Integer sort;

    /**
     * Enable market buy
     */
    @ApiModelProperty(value = "Enable market buy")
    private Integer enableMarketBuy;

    /**
     * Enable market sell
     */
    @ApiModelProperty(value = "Enable market sell")
    private Integer enableMarketSell;

    /**
     * Minimum sell order price
     */
    @ApiModelProperty(value = "Minimum sell order price")
    private BigDecimal minSellPrice;

    /**
     * Default is 0, 1 means recommended
     */
    @ApiModelProperty(value = "Default is 0, 1 means recommended")
    private Integer flag;

    /**
     * Maximum number of simultaneous active orders allowed, 0 means unlimited
     */
    @ApiModelProperty(value = "Maximum number of simultaneous active orders allowed, 0 means unlimited")
    private Integer maxTradingOrder;

    /**
     * Auto cancel time for orders in seconds, 0 means no expiration
     */
    @ApiModelProperty(value = "Auto cancel time for orders in seconds, 0 means no expiration")
    private Integer maxTradingTime;

    /**
     * Trading type, specific to B2C2
     */
    @ApiModelProperty(value = "Trading type, specific to B2C2")
    private String instrument;

    /**
     * Minimum order turnover
     */
    @ApiModelProperty(value = "Minimum order turnover")
    private BigDecimal minTurnover;

    /**
     * Maximum order quantity
     */
    @ApiModelProperty(value = "Maximum order quantity")
    private BigDecimal maxVolume;

    /**
     * Minimum order quantity
     */
    @ApiModelProperty(value = "Minimum order quantity")
    private BigDecimal minVolume;

    private Integer zone;

    /**
     * Settlement time
     */
    @ApiModelProperty(value = "Settlement time")
    private String clearTime;

    /**
     * End time
     */
    @ApiModelProperty(value = "End time")
    private String endTime;

    /**
     * Allocated issue price
     */
    @ApiModelProperty(value = "Allocated issue price")
    private BigDecimal publishPrice;

    /**
     * Issue activity type: 1 - No activity, 2 - Flash sale issue, 3 - Allocated issue
     */
    @ApiModelProperty(value = "Issue activity type: 1 - No activity, 2 - Flash sale issue, 3 - Allocated issue")
    private Integer publishType;

    /**
     * Start time
     */
    @ApiModelProperty(value = "Start time")
    private String startTime;

    /**
     * Is tradable
     */
    @ApiModelProperty(value = "Is tradable")
    private Integer exchangeable;

    /**
     * Activity issuance amount
     */
    @ApiModelProperty(value = "Activity issuance amount")
    private BigDecimal publishAmount;

    /**
     * Frontend visibility status
     */
    @ApiModelProperty(value = "Frontend visibility status")
    private Integer visible;

    /**
     * Maximum buy order price
     */
    @ApiModelProperty(value = "Maximum buy order price")
    private BigDecimal maxBuyPrice;

    /**
     * Robot type
     */
    @ApiModelProperty(value = "Robot type")
    private Integer robotType;

    /**
     * Allow buy
     */
    @ApiModelProperty(value = "Allow buy")
    private Integer enableBuy;

    /**
     * Allow sell
     */
    @ApiModelProperty(value = "Allow sell")
    private Integer enableSell;

    @TableField(exist = false)
    private Long currentTime;

    /**
     * Trading engine status (0: unavailable, 1: available)
     */
    @ApiModelProperty(value = "Trading engine status (0: unavailable, 1: available)")
    @TableField(exist = false)
    private int engineStatus = 0;

    /**
     * Market engine status (0: unavailable, 1: available)
     */
    @ApiModelProperty(value = "Market engine status (0: unavailable, 1: available)")
    @TableField(exist = false)
    private int marketEngineStatus = 0;

    /**
     * Trading bot status (0: not running, 1: running)
     */
    @ApiModelProperty(value = "Trading bot status (0: not running, 1: running)")
    @TableField(exist = false)
    private int exEngineStatus = 0;

    /**
     * BID price threshold (e.g., 0.8 means 80% of highest price)
     * Used to filter orders in TradePlate to prevent list from growing too large
     */
    @ApiModelProperty(value = "BID price threshold (e.g., 0.8 means 80% of highest price)")
    private BigDecimal bidPriceThreshold = BigDecimal.ZERO;

    /**
     * ASK price threshold (e.g., 1.2 means 120% of lowest price)
     * Used to filter orders in TradePlate to prevent list from growing too large
     */
    @ApiModelProperty(value = "ASK price threshold (e.g., 1.2 means 120% of lowest price)")
    private BigDecimal askPriceThreshold = BigDecimal.ZERO;

}
