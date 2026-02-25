package com.wikex.wikex.earn.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Fixed-term Financial Savings Order Table
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LockedSavingsOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * ID from locked_savings_activity table
     */
    private Long lockedId;

    /**
     * member_id
     */
    private Long memberId;

    /**
     * Coin unit
     */
    private String coinUnit;

    /**
     * Duration
     */
    private Integer duration;

    /**
     * Rate of return
     */
    private BigDecimal rate;

    /**
     * Fixed-term amount
     */
    private BigDecimal num;

    /**
     * Earnings amount
     */
    private BigDecimal earnNum;

    /**
     * Status: 0 - In progress, 1 - Completed
     */
    private Integer status;

    /**
     * Redemption date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * Interest start date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
