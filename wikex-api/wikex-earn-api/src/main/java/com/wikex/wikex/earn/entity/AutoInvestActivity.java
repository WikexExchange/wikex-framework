package com.wikex.wikex.earn.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Auto-Investment Activity (Financial DCA )
 * </p>
 * Represents the configuration of an automated recurring investment plan,
 * including target coin, base coin, interest rates, fees, limits, and status.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AutoInvestActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Target coin unit (e.g., BTC, ETH)
     */
    private String coinUnit;

    /**
     * Base coin unit used for purchase (e.g., USDT)
     */
    private String baseUnit;

    /**
     * JSON string representing interest rates and investment periods
     */
    private String rate;

    /**
     * Transaction fee
     */
    private BigDecimal fee;

    /**
     * Minimum investment amount
     */
    private BigDecimal num;

    /**
     * Sort order for display
     */
    private Integer sort;

    /**
     * Status:
     * 0 - Disabled
     * 1 - Enabled
     */
    private Integer status;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Last update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * Current price of the base coin (transient, not persisted)
     */
    @TableField(exist = false)
    private BigDecimal price;
}
