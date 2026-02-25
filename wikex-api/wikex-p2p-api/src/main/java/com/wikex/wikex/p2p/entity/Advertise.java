package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * P2P Advertisement Entity
 * Represents a buy or sell ad in the P2P trading system.
 * Includes pricing, limits, status, and owner details.
 * 
 */
@ApiModel(value = "P2P Advertisement")
@Data
@EqualsAndHashCode(callSuper = false)
public class Advertise implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Ad type: 0 = Buy, 1 = Sell")
    private Integer advertiseType;

    @ApiModelProperty(value = "Enable auto-reply: 0 = No, 1 = Yes")
    private Integer auto;

    @ApiModelProperty(value = "Auto-reply message")
    private String autoword;

    @ApiModelProperty(value = "Coin unit")
    private String coinUnit;

    @ApiModelProperty(value = "Ad creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "Amount currently in transaction")
    private BigDecimal dealAmount;

    @ApiModelProperty(value = "Ad level: 0 = Normal, 1 = Premium")
    private Integer level;

    @ApiModelProperty(value = "Total amount (string representation)")
    private String limitMoney;

    @ApiModelProperty(value = "Maximum transaction limit per order")
    private BigDecimal maxLimit;

    @ApiModelProperty(value = "Minimum transaction limit per order")
    private BigDecimal minLimit;

    @ApiModelProperty(value = "Planned total quantity")
    private BigDecimal number;

    @ApiModelProperty(value = "Payment methods (comma-separated)")
    private String payMode;

    @ApiModelProperty(value = "Premium percentage")
    private BigDecimal premiseRate;

    @ApiModelProperty(value = "Trading price")
    private BigDecimal price;

    @ApiModelProperty(value = "Price type: 0 = Fixed, 1 = Floating")
    private Integer priceType;

    @ApiModelProperty(value = "Remaining quantity")
    private BigDecimal remainAmount;

    @ApiModelProperty(value = "Remarks")
    private String remark;

    @ApiModelProperty(value = "Ad status: 0 = Active, 1 = Inactive, 2 = Closed (Deleted)")
    private Integer status;

    @ApiModelProperty(value = "Review status: 0 = Pending, 1 = Approved, 2 = Rejected")
    private Integer auditStatus;

    @ApiModelProperty(value = "Reason for rejection (if applicable)")
    private String auditRemark;

    @ApiModelProperty(value = "Payment time limit (in minutes)")
    private Integer timeLimit;

    @ApiModelProperty(value = "Last updated time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "Username of the advertiser")
    private String username;

    @ApiModelProperty(value = "Version")
    private Long version;

    @ApiModelProperty(value = "Coin ID")
    private Long coinId;

    @ApiModelProperty(value = "Country code")
    private String country;

    @ApiModelProperty(value = "Owner's member ID")
    private Long memberId;

    @ApiModelProperty(value = "Local currency code")
    @TableField(exist = false)
    private String localCurrency;

    @ApiModelProperty(value = "Final calculated price")
    @TableField(exist = false)
    private BigDecimal finalPrice;

    @ApiModelProperty(value = "Owner's avatar URL")
    @TableField(exist = false)
    private String avatar;

    @ApiModelProperty(value = "Owner's membership level")
    @TableField(exist = false)
    private Integer memberLevel;

    @ApiModelProperty(value = "Number of completed transactions")
    @TableField(exist = false)
    private Integer transactions;

    @ApiModelProperty(value = "Owner's real name")
    @TableField(exist = false)
    private String realName;

}
