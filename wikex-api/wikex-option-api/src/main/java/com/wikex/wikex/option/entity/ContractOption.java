package com.wikex.wikex.option.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.ContractOptionResult;
import com.wikex.wikex.constant.ContractOptionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Contract Option Record
 * </p>
 */
@ApiModel(value = "Contract Option Record")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractOption implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Closing price
     */
    @ApiModelProperty(value = "Closing price")
    private BigDecimal closePrice;

    @ApiModelProperty(value = "Close time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Long closeTime;

    @ApiModelProperty(value = "Create time")
    private Long createTime;

    /**
     * Total amount in the buy-up prize pool
     */
    @ApiModelProperty(value = "Total amount in the buy-up prize pool")
    private BigDecimal initBuy;

    /**
     * Total amount in the buy-down prize pool
     */
    @ApiModelProperty(value = "Total amount in the buy-down prize pool")
    private BigDecimal initSell;

    /**
     * Opening price
     */
    @ApiModelProperty(value = "Opening price")
    private BigDecimal openPrice;

    @ApiModelProperty(value = "Open time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Long openTime;

    /**
     * Contract serial number
     */
    @ApiModelProperty(value = "Contract serial number")
    private Integer optionNo;

    /**
     * Result of the current round
     */
    @ApiModelProperty(value = "Result of the current round")
    private ContractOptionResult result;

    /**
     * Status of the current contract
     */
    @ApiModelProperty(value = "Status of the current contract")
    private ContractOptionStatus status;

    @ApiModelProperty(value = "Trading pair symbol")
    private String symbol;

    /**
     * Total amount in the buy-up prize pool
     */
    @ApiModelProperty(value = "Total amount in the buy-up prize pool")
    private BigDecimal totalBuy;

    /**
     * Number of buy-up participants
     */
    @ApiModelProperty(value = "Number of buy-up participants")
    private Integer totalBuyCount;

    /**
     * Total profit/loss
     */
    @ApiModelProperty(value = "Total profit/loss")
    private BigDecimal totalPl;

    /**
     * Total amount in the buy-down prize pool
     */
    @ApiModelProperty(value = "Total amount in the buy-down prize pool")
    private BigDecimal totalSell;

    /**
     * Number of buy-down participants
     */
    @ApiModelProperty(value = "Number of buy-down participants")
    private Integer totalSellCount;

    /**
     * Preset price
     */
    @ApiModelProperty(value = "Preset price")
    private BigDecimal presetPrice;

}
