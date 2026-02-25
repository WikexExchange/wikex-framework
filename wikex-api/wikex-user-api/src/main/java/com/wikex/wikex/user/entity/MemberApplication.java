package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.AuditStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * Member audit/application information table
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Application Information")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Primary key ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Audit status: 0 = pending, 1 = failed, 2 = success
     */
    @ApiModelProperty(value = "Audit status (0: pending, 1: failed, 2: success)")
    private AuditStatus auditStatus;

    @ApiModelProperty(value = "Creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "ID card number")
    private String idCard;

    @ApiModelProperty(value = "ID card image - front")
    private String identityCardImgFront;

    @ApiModelProperty(value = "ID card image - in hand")
    private String identityCardImgInHand;

    @ApiModelProperty(value = "ID card image - reverse")
    private String identityCardImgReverse;

    @ApiModelProperty(value = "KYC level 2 video certification URL")
    private String videoUrl;

    @ApiModelProperty(value = "Real name")
    private String realName;

    @ApiModelProperty(value = "Rejection reason")
    private String rejectReason;

    /**
     * Authentication type: 0 = ID card, 1 = passport, 2 = driver’s license
     */
    @ApiModelProperty(value = "Authentication type (0: ID card, 1: passport, 2: driver's license)")
    private Integer type;

    @ApiModelProperty(value = "Last update time")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    @ApiModelProperty(value = "KYC status/level")
    private Integer kycStatus;

    @ApiModelProperty(value = "Video certification random number")
    private String videoRandom;
}
