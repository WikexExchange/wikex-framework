package com.wikex.wikex.second.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Second contract cycle
 * </p>
 *
 */
@ApiModel(value = "Second contract cycle")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractSecondCycle implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * Minimum amount
     */
    @ApiModelProperty(value = "Minimum amount")
    private BigDecimal minAmount;

    /**
     * Maximum amount
     */
    @ApiModelProperty(value = "Maximum amount")
    private BigDecimal maxAmount;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Update time
     */
    @ApiModelProperty(value = "Update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
