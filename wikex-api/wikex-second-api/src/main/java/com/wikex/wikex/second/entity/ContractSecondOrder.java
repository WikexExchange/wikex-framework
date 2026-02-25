package com.wikex.wikex.second.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.ContractSecondOrderDirection;
import com.wikex.wikex.constant.ContractSecondOrderResult;
import com.wikex.wikex.constant.ContractSecondOrderStatus;
import com.wikex.wikex.constant.ContractSecondOrderType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Second contract order
 * </p>
 *
 */
@ApiModel(value = "Second contract order")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractSecondOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Bet amount
     */
    @ApiModelProperty(value = "Bet amount")
    private BigDecimal betAmount;

    private String coinSymbol;

    private String symbol;

    /**
     * Direction
     */
    @ApiModelProperty(value = "Direction")
    private ContractSecondOrderDirection direction;

    /**
     * Fee
     */
    @ApiModelProperty(value = "Fee")
    private BigDecimal fee;

    /**
     * User ID
     */
    @ApiModelProperty(value = "User ID")
    private Long memberId;

    /**
     * Cycle ID
     */
    @ApiModelProperty(value = "Cycle ID")
    private Long cycleId;

    /**
     * Cycle odds
     */
    @ApiModelProperty(value = "Cycle odds")
    private BigDecimal cycleRate;

    /**
     * Cycle duration (seconds)
     */
    @ApiModelProperty(value = "Cycle duration (seconds)")
    private Long cycleLength;

    /**
     * Open price
     */
    @ApiModelProperty(value = "Open price")
    private BigDecimal openPrice;

    /**
     * Close price
     */
    @ApiModelProperty(value = "Close price")
    private BigDecimal closePrice;

    /**
     * Pre-set close price
     */
    @ApiModelProperty(value = "Pre-set close price")
    private BigDecimal preClosePrice;

    /**
     * Whether indemnity: 0 = No, 1 = Yes
     */
    @ApiModelProperty(value = "Whether indemnity: 0 = No, 1 = Yes")
    private ContractSecondOrderType type;

    /**
     * Result: 0 = No result, 1 = Success, 2 = Fail, 3 = Cancel
     */
    @ApiModelProperty(value = "Result: 0 = No result, 1 = Success, 2 = Fail, 3 = Cancel")
    private ContractSecondOrderResult result;

    /**
     * Win amount
     */
    @ApiModelProperty(value = "Win amount")
    private BigDecimal winAmount;

    /**
     * Order status: 0 = Entrusting, 1 = Holding, 2 = Closed, 3 = Cancelled
     */
    @ApiModelProperty(value = "Order status: 0 = Entrusting, 1 = Holding, 2 = Closed, 3 = Cancelled")
    private ContractSecondOrderStatus status;

    /**
     * Close time
     */
    @ApiModelProperty(value = "Close time")
    private Date closeTime;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    private Date createTime;

    /**
     * Update time
     */
    @ApiModelProperty(value = "Update time")
    private Date updateTime;

}
