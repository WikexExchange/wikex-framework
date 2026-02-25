package com.wikex.wikex.swap.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Perpetual Contract Entrust Order
 * </p>
 *
 * @author sulinxin
 * @since 2021-08-23
 */
@ApiModel(value = "Perpetual Contract Entrust Order")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractOrderEntrust implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Settlement (base) coin symbol, e.g. USDT
     */
    @ApiModelProperty(value = "Settlement (base) coin symbol, e.g. USDT")
    private String baseSymbol;

    /**
     * Close position fee
     */
    @ApiModelProperty(value = "Close position fee")
    private BigDecimal closeFee;

    /**
     * Trading coin symbol
     */
    @ApiModelProperty(value = "Trading coin symbol")
    private String coinSymbol;

    /**
     * contract_coin table id
     */
    @ApiModelProperty(value = "contract_coin table id")
    private Long contractId;

    /**
     * Contract order entrust ID (appears mislabelled as creation time)
     */
    @ApiModelProperty(value = "Contract order entrust ID")
    private String contractOrderEntrustId;

    /**
     * Creation timestamp (epoch millis)
     */
    @ApiModelProperty(value = "Creation timestamp")
    private Long createTime;

    /**
     * Price at the time of order placement
     */
    @ApiModelProperty(value = "Price at the time of order placement")
    private BigDecimal currentPrice;

    /**
     * Direction (e.g. BUY or SELL)
     */
    @ApiModelProperty(value = "Direction")
    private ContractOrderDirection direction;

    /**
     * Entrust price
     */
    @ApiModelProperty(value = "Entrust price")
    private BigDecimal entrustPrice;

    /**
     * Entrust order type
     */
    @ApiModelProperty(value = "Entrust order type")
    private ContractOrderEntrustType entrustType;

    /**
     * Whether this is a trigger order (planned entrust)
     */
    @ApiModelProperty(value = "Whether this is a trigger order (planned entrust)")
    private Integer isBlast;

    /**
     * Whether the order originated from spot trading
     */
    @ApiModelProperty(value = "Whether the order originated from spot trading")
    private Integer isFromSpot;

    /**
     * User ID
     */
    @ApiModelProperty(value = "User ID")
    private Long memberId;

    /**
     * Open position fee
     */
    @ApiModelProperty(value = "Open position fee")
    private BigDecimal openFee;

    /**
     * Position mode (e.g., cross margin or isolated margin)
     */
    @ApiModelProperty(value = "Position mode")
    private ContractOrderPattern patterns;

    /**
     * Principal amount
     */
    @ApiModelProperty(value = "Principal amount")
    private BigDecimal principalAmount;

    /**
     * Principal unit (base currency for principal)
     */
    @ApiModelProperty(value = "Principal unit (base currency for principal)")
    private String principalUnit;

    /**
     * Profit and loss
     */
    @ApiModelProperty(value = "Profit and loss")
    private BigDecimal profitAndLoss;

    /**
     * Contract face value (lot size)
     */
    @ApiModelProperty(value = "Contract face value")
    private BigDecimal shareNumber;

    /**
     * Entrust status: 0 - Entrusting, 1 - Cancelled, 2 - Failed, 3 - Successful
     */
    @ApiModelProperty(value = "Entrust status: 0 - Entrusting, 1 - Cancelled, 2 - Failed, 3 - Successful")
    private ContractOrderEntrustStatus status;

    /**
     * Trading pair symbol
     */
    @ApiModelProperty(value = "Trading pair symbol")
    private String symbol;

    /**
     * Average traded price
     */
    @ApiModelProperty(value = "Average traded price")
    private BigDecimal tradedPrice;

    /**
     * Entrust volume (amount ordered)
     */
    @ApiModelProperty(value = "Entrust volume")
    private BigDecimal tradedVolume;

    /**
     * Trigger price (mandatory if order type is planned/limit)
     */
    @ApiModelProperty(value = "Trigger price (mandatory if order type is planned/limit)")
    private BigDecimal triggerPrice;

    /**
     * Triggering timestamp
     */
    @ApiModelProperty(value = "Triggering timestamp")
    private Long triggeringTime;

    /**
     * Entrust type: 0 - Market price, 1 - Limit price, 2 - Planned entrust
     */
    @ApiModelProperty(value = "Entrust type: 0 - Market price, 1 - Limit price, 2 - Planned entrust")
    private ContractOrderType type;

    /**
     * Entrust volume (amount)
     */
    @ApiModelProperty(value = "Entrust volume")
    private BigDecimal volume;

    /**
     * Whether commission has been rewarded
     */
    @ApiModelProperty(value = "Whether commission has been rewarded")
    private Integer isReward;

    /**
     * Position ID
     */
    @ApiModelProperty(value = "Position ID")
    private Long positionId;

    /**
     * Leverage multiplier
     */
    private BigDecimal leverage;
}
