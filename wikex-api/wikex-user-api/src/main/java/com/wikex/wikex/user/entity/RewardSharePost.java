package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@ApiModel(value = "Reward Share Post")
@Data
@EqualsAndHashCode(callSuper = false)
public class RewardSharePost implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "member id")
    private Long memberId;

    @ApiModelProperty(value = "type: 0 trading, 2 inviter, 3 commission, 4 pnl")
    @TableField( "`type`")
    private Integer type;

    @ApiModelProperty(value = "period: week|month")
    @TableField( "`period`")
    private String period;

    @ApiModelProperty(value = "period index: weekNo or monthNo")
    private Integer periodNo;

    @ApiModelProperty(value = "year_no")
    private Integer yearNo;

    @ApiModelProperty(value = "post url")
    private String postUrl;

    @ApiModelProperty(value = "status 0-pending 1-awaiting_approval 2-verified")
    @TableField( "`status`")
    private Integer status;

    @ApiModelProperty(value = "create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
