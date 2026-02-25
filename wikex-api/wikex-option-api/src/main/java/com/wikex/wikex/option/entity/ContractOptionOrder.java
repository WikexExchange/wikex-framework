package com.wikex.wikex.option.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.constant.ContractOptionOrderResult;
import com.wikex.wikex.constant.ContractOptionOrderStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Contract Option Order
 * </p>
 */
@ApiModel(value = "Contract Option Order")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractOptionOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Base currency")
    private String baseSymbol;

    /**
     * Bet amount
     */
    @ApiModelProperty(value = "Bet amount")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "Currency unit")
    private String coinSymbol;

    @ApiModelProperty(value = "Creation time")
    private Long createTime;

    /**
     * Direction
     */
    @ApiModelProperty(value = "Direction")
    private ContractOptionOrderDirection direction;

    /**
     * Fee
     */
    @ApiModelProperty(value = "Fee")
    private BigDecimal fee;

    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    @ApiModelProperty(value = "Contract option ID")
    private Long optionId;

    /**
     * Contract sequence number
     */
    @ApiModelProperty(value = "Contract sequence number")
    private Integer optionNo;

    /**
     * Participation result
     */
    @ApiModelProperty(value = "Participation result")
    private ContractOptionOrderResult result;

    /**
     * Winning bonus
     */
    @ApiModelProperty(value = "Winning bonus")
    private BigDecimal rewardAmount;

    /**
     * Order status
     */
    @ApiModelProperty(value = "Order status")
    private ContractOptionOrderStatus status;

    @ApiModelProperty(value = "Symbol")
    private String symbol;

    /**
     * Commission
     */
    @ApiModelProperty(value = "Commission")
    private BigDecimal winFee;

}
