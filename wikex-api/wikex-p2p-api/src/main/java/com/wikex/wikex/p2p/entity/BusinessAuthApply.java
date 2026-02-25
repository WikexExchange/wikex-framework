package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.user.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OTC Merchant Certification Application
 * Represents a merchant's application for OTC (Over-The-Counter) business certification.
 * Tracks deposit, status, audit information, and linked user data.
 *
 * Author: markchao
 * Since: 2021-08-21
 */
@ApiModel(value = "OTC Merchant Certification Application")
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessAuthApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Primary key ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Deposit amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "Audit time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditingTime;

    @ApiModelProperty(value = "Authentication information")
    private String authInfo;

    @ApiModelProperty(value = "Certified merchant status: 0=Not Certified, 1=Pending Certification Review, 2=Certification Approved, 3=Certification Failed, 4=Deposit Insufficient, 5=Pending Deposit Withdrawal Review, 6=Deposit Withdrawal Failed, 7=Deposit Withdrawal Approved")
    private CertifiedBusinessStatus certifiedBusinessStatus;

    @ApiModelProperty(value = "Application creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "Deposit record ID")
    private String depositRecordId;

    @ApiModelProperty(value = "Reason for certification failure")
    private String detail;

    @ApiModelProperty(value = "Last update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "BusinessAuthDeposit table ID")
    private Long businessAuthDepositId;

    @ApiModelProperty(value = "User ID")
    private Long memberId;

    @TableField(exist = false)
    @ApiModelProperty(value = "Associated member information")
    private Member member;

    @TableField(exist = false)
    @ApiModelProperty(value = "Associated deposit information")
    private BusinessAuthDeposit businessAuthDeposit;


}
