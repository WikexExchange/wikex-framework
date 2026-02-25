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
 * Fixed-term Financial Savings Table
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LockedSavingsActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Fixed-term activity name (can be duplicate)
     */
    private String name;

    /**
     * Rate of return
     */
    private BigDecimal rate;

    /**
     * Duration (days)
     */
    private Integer duration;

    /**
     * Coin unit
     */
    private String coinUnit;

    /**
     * Minimum investment amount
     */
    private BigDecimal num;

    /**
     * Sort order
     */
    private Integer sort;

    /**
     * Status: 0 - Unavailable, 1 - Available
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
