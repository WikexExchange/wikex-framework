package com.wikex.wikex.p2p.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.user.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OTC Merchant Exit Application
 * Represents a merchant’s request to exit OTC certification,
 * including deposit refund details and application status.
 *
 * Author: markchao
 * Since: 2021-08-21
 */
@ApiModel(value = "OTC Merchant Exit Application")
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessCancelApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelApplyTime;

    private String depositRecordId;

    private String detail;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handleTime;

    private String reason;

    private Integer status;

    private Long memberId;

    @TableField(exist = false)
    private DepositRecord depositRecord;

    @TableField(exist = false)
    private Member member;
}
