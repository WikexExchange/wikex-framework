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
 * Financial Savings Statistics Table
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LockedSavingsStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Financial savings amount
     */
    private BigDecimal num;

    private Long memberId;

    /**
     * Coin unit
     */
    private String coinSymbol;

    /**
     * Total accumulated profit
     */
    private BigDecimal earnNum;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
