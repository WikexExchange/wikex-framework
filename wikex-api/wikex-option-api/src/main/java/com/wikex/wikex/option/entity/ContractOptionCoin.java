package com.wikex.wikex.option.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Transient;

/**
 * <p>
 * Contract Option Trading Pair
 * </p>
 *
 */
@ApiModel(value = "Contract Option Trading Pair")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractOptionCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Trading pair symbol")
    @TableId(value = "symbol", type = IdType.INPUT)
    private String symbol;

    @ApiModelProperty(value = "Amount")
    private String amount;

    /**
     * Base currency decimal precision
     */
    @ApiModelProperty(value = "Base currency decimal precision")
    private Integer baseCoinScale;

    @ApiModelProperty(value = "Base currency / settlement currency")
    private String baseSymbol;

    /**
     * Time interval from opening to closing
     */
    @ApiModelProperty(value = "Time interval from opening to closing")
    private Integer closeTimeGap;

    /**
     * Trading currency decimal precision
     */
    @ApiModelProperty(value = "Trading currency decimal precision")
    private Integer coinScale;

    @ApiModelProperty(value = "Trading currency symbol")
    private String coinSymbol;

    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Status
     */
    @ApiModelProperty(value = "Status")
    private Integer enable;

    /**
     * Whether buying up is allowed
     */
    @ApiModelProperty(value = "Whether buying up is allowed")
    private Integer enableBuy = BooleanEnum.IS_TRUE.getCode();

    /**
     * Whether buying down is allowed
     */
    @ApiModelProperty(value = "Whether buying down is allowed")
    private Integer enableSell = BooleanEnum.IS_TRUE.getCode();

    /**
     * Odds
     */
    @ApiModelProperty(value = "Odds")
    private BigDecimal oods;

    /**
     * Opening fee rate
     */
    @ApiModelProperty(value = "Opening fee rate")
    private BigDecimal feePercent;

    /**
     * Initial buy-up prize pool amount
     */
    @ApiModelProperty(value = "Initial buy-up prize pool amount")
    private BigDecimal initBuyReward;

    /**
     * Initial buy-down prize pool amount
     */
    @ApiModelProperty(value = "Initial buy-down prize pool amount")
    private BigDecimal initSellReward;

    /**
     * Latest contract number
     */
    @ApiModelProperty(value = "Latest contract number")
    private Integer maxOptionNo;

    @ApiModelProperty(value = "Name")
    private String name;

    /**
     * Ignore price fluctuation percentage
     */
    @ApiModelProperty(value = "Ignore price fluctuation percentage")
    private BigDecimal ngnorePercent;

    /**
     * Time interval from start to opening
     */
    @ApiModelProperty(value = "Time interval from start to opening")
    private Integer openTimeGap;

    /**
     * Sort order
     */
    @ApiModelProperty(value = "Sort order")
    private Integer sort;

    /**
     * Tie handling method
     */
    @ApiModelProperty(value = "Tie handling method")
    private Integer tiedType;

    /**
     * Predicted total profit of the contract
     */
    @ApiModelProperty(value = "Predicted total profit of the contract")
    private BigDecimal totalProfit;

    /**
     * Frontend visible status
     */
    @ApiModelProperty(value = "Frontend visible status")
    private Integer visible;

    /**
     * Winner's fee rate
     */
    @ApiModelProperty(value = "Winner's fee rate")
    private BigDecimal winFeePercent;

    @ApiModelProperty(value = "Current time")
    @TableField(exist = false)
    private Long currentTime;

}
