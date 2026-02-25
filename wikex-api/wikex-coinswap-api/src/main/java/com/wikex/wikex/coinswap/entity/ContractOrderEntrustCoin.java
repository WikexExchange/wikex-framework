package com.wikex.wikex.coinswap.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Perpetual Contract Entrust Order
 * </p>
 *
 * Author: sulinxin
 * Since: 2021-08-23
 */
@ApiModel(value = "Perpetual Contract Entrust Order")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractOrderEntrustCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String baseSymbol;

    /**
     * Closing fee
     */
    @ApiModelProperty(value = "Closing fee")
    private BigDecimal closeFee;

    private String coinSymbol;

    private Long contractId;

    private String contractOrderEntrustId;

    private Long createTime;

    /**
     * Price at the time of order placement
     */
    @ApiModelProperty(value = "Price at the time of order placement")
    private BigDecimal currentPrice;

    /**
     * Direction
     */
    @ApiModelProperty(value = "Direction")
    private ContractOrderDirection direction;

    private BigDecimal entrustPrice;

    /**
     * Entrust order type
     */
    @ApiModelProperty(value = "Entrust order type")
    private ContractOrderEntrustType entrustType;

    /**
     * Whether it is an entrust order triggered by liquidation
     */
    @ApiModelProperty(value = "Whether it is an entrust order triggered by liquidation")
    private Integer isBlast;

    /**
     * Whether it is an entrust order from spot market
     */
    @ApiModelProperty(value = "Whether it is an entrust order from spot market")
    private Integer isFromSpot;

    private Long memberId;

    /**
     * Opening fee
     */
    @ApiModelProperty(value = "Opening fee")
    private BigDecimal openFee;

    /**
     * Position mode
     */
    @ApiModelProperty(value = "Position mode")
    private ContractOrderPattern patterns;

    /**
     * Principal amount
     */
    @ApiModelProperty(value = "Principal amount")
    private BigDecimal principalAmount;

    private String principalUnit;

    /**
     * Profit and loss
     */
    @ApiModelProperty(value = "Profit and loss")
    private BigDecimal profitAndLoss;

    /**
     * Contract face value
     */
    @ApiModelProperty(value = "Contract face value")
    private BigDecimal shareNumber;

    @ApiModelProperty(value = "Status")
    private ContractOrderEntrustStatus status;

    @ApiModelProperty(value = "Trading pair symbol")
    private String symbol;

    @ApiModelProperty(value = "Executed price")
    private BigDecimal tradedPrice;

    /**
     * Executed volume
     */
    @ApiModelProperty(value = "Executed volume")
    private BigDecimal tradedVolume;

    @ApiModelProperty(value = "Trigger price (required if order type is planned/limit)")
    private BigDecimal triggerPrice;

    @ApiModelProperty(value = "Trigger time")
    private Long triggeringTime;

    @ApiModelProperty(value = "Order type")
    private ContractOrderType type;

    /**
     * Entrusted volume
     */
    @ApiModelProperty(value = "Entrusted volume")
    private BigDecimal volume;

    /**
     * Whether commission has been returned
     */
    @ApiModelProperty(value = "Whether commission has been returned")
    private Integer isReward;
}
