package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "Reward Period Type")
@Data
@EqualsAndHashCode(callSuper = false)
public class RewardPeriodType implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "period")
    private String period;

    @ApiModelProperty(value = "year_no")
    private Integer yearNo;

    @ApiModelProperty(value = "period_no")
    private Integer periodNo;

    @ApiModelProperty(value = "type")
    @TableField( "`type`")
    private Integer type;
}
