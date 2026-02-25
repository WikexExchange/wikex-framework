package com.wikex.wikex.earn.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Transient;

/**
 * <p>
 * Auto-Invest Activity Plan Table
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AutoInvestPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * ID from locked_savings_activity table
     */
    private Long activityId;

    /**
     * member_id
     */
    private Long memberId;

    /**
     * Target coin unit
     */
    private String coinUnit;

    /**
     * Base coin unit used
     */
    private String baseUnit;

    /**
     * Cycle
     */
    private Integer cycle;

    /**
     * Auto-invest quantity (per time)
     */
    private BigDecimal amount;

    /**
     * Average price
     */
    private BigDecimal averagePrice;

    /**
     * Cumulative investment amount (USDT)
     */
    private BigDecimal cumulativeAmount;

    /**
     * Cumulative purchase quantity
     */
    private BigDecimal cumulativeBuyAmount;

    /**
     * Status: 0 - Plan enabled, 1 - Plan disabled
     */
    private Integer status;

    /**
     * Delete flag: 0 - Normal, 1 - Deleted
     */
    private Integer delFlag;

    /**
     * Next auto-invest time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextTime;

    /**
     * Plan start time
     */
    private String startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * Unrealized profit/loss = (Spot price - Average cost) * Holding quantity
     */
    @TableField(exist = false)
    private String profitLoss;
    /**
     * Reported ROI = Unrealized profit/loss / Invested amount
     */
    @TableField(exist = false)
    private String roi;
}
