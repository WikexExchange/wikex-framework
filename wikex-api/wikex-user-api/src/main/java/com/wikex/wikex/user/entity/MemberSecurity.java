package com.wikex.wikex.user.entity;

import com.wikex.wikex.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


@ApiModel(value = "User Center Authentication")
@Builder
@Data
public class MemberSecurity {

    @ApiModelProperty(value = "Username")
    private String username;

    @ApiModelProperty(value = "ID")
    private long id;

    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "Is Real Name Verified")
    private Integer realVerified = BooleanEnum.IS_FALSE.getCode();

    @ApiModelProperty(value = "Is Email Verified")
    private Integer emailVerified = BooleanEnum.IS_FALSE.getCode();

    @ApiModelProperty(value = "Is Phone Verified")
    private Integer phoneVerified = BooleanEnum.IS_FALSE.getCode();

    @ApiModelProperty(value = "Is Login Verified")
    private Integer loginVerified = BooleanEnum.IS_FALSE.getCode();

    @ApiModelProperty(value = "Is Funds Verified")
    private Integer fundsVerified = BooleanEnum.IS_FALSE.getCode();

    @ApiModelProperty(value = "Is Under Real Name Audit")
    private Integer realAuditing;

    @ApiModelProperty(value = "Mobile Phone Number")
    private String mobilePhone;

    @ApiModelProperty(value = "Email")
    private String email;

    @ApiModelProperty(value = "Real Name")
    private String realName;

    @ApiModelProperty(value = "Real Name Rejection Reason")
    private String realNameRejectReason;

    @ApiModelProperty(value = "ID Card Number")
    private String idCard;

    @ApiModelProperty(value = "Avatar URL")
    private String avatar;

    @ApiModelProperty(value = "Is Account Verified")
    private Integer accountVerified = BooleanEnum.IS_FALSE.getCode();

    @ApiModelProperty(value = "Google Auth Status")
    private Integer googleStatus = BooleanEnum.IS_FALSE.getCode();
}
