package com.wikex.wikex.p2p.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.AdvertiseType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdvertiseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Advertisement type 0: buy 1: sell
     */
    private AdvertiseType advertiseType;

    /**
     * Whether auto-reply is enabled 0 no 1 yes
     */
    private Integer auto;

    /**
     * Auto-reply content
     */
    private String autoword;

    /**
     * Coin unit
     */
    private String coinUnit;

    /**
     * Advertisement creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Amount in transaction
     */
    private BigDecimal dealAmount;

    /**
     * Advertisement level 0 normal 1 premium
     */
    private Integer level;

    /**
     * Amount limit (money)
     */
    private String limitMoney;

    /**
     * Maximum single transaction amount
     */
    private BigDecimal maxLimit;

    /**
     * Minimum single transaction amount
     */
    private BigDecimal minLimit;

    /**
     * Planned quantity
     */
    private BigDecimal number;

    /**
     * Payment methods (separated by English commas)
     */
    private String payMode;

    /**
     * Premium percentage
     */
    private BigDecimal premiseRate;

    /**
     * Transaction price
     */
    private BigDecimal price;

    /**
     * Price type 0 fixed 1 variable
     */
    private Integer priceType;

    /**
     * Remaining planned quantity
     */
    private BigDecimal remainAmount;

    /**
     * Remark
     */
    private String remark;

    /**
     * Advertisement status 0 active 1 inactive 2 closed (deleted)
     */
    private Integer status;

    /**
     * Audit status 0 pending 1 approved 2 rejected
     */
    private Integer auditStatus;

    /**
     * Audit rejection remark
     */
    private String auditRemark;

    /**
     * Payment deadline (minutes)
     */
    private Integer timeLimit;

    /**
     * Advertisement last update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * Username
     */
    private String username;

    /**
     * Version
     */
    private Long version;

    /**
     * Coin ID
     */
    private Long coinId;

    /**
     * Country
     */
    private String country;

    /**
     * Advertisement owner ID
     */
    private Long memberId;

    private BigDecimal minWithdrawAmount;

}
