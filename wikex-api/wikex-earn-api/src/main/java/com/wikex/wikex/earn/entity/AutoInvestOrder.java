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
 * Auto-Invest Activity Order Table
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AutoInvestOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * ID from locked_savings_plan table
     */
    private Long planId;

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
     * Auto-invest quantity
     */
    private BigDecimal num;

    /**
     * Auto-invest amount
     */
    private BigDecimal baseNum;

    /**
     * Transaction fee
     */
    private BigDecimal fee;

    /**
     * Status: 0 - Success, 1 - Failure
     */
    private Integer status;

    /**
     * Auto-invest time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date investTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
