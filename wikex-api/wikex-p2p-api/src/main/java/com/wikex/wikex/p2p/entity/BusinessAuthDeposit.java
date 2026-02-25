package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.user.entity.Coin;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OTC Merchant Deposit Information
 * Represents the pledged cryptocurrency and amount held as a deposit
 * for OTC merchant certification.
 *
 * Author: markchao
 * Since: 2021-08-21
 */
@ApiModel(value = "OTC Merchant Deposit Information")
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessAuthDeposit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Primary key ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Deposit amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "Status: 0=Normal, 1=Invalid")
    private Integer status;

    @ApiModelProperty(value = "Admin user ID (operator who recorded the deposit)")
    private Long adminId;

    @ApiModelProperty(value = "Pledged coin ID")
    private String coinId;

    @TableField(exist = false)
    @ApiModelProperty(value = "Associated coin details")
    private Coin coin;
}
