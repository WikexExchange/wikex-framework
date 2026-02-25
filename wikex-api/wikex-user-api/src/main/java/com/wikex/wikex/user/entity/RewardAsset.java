package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "Reward Asset")
@Data
@EqualsAndHashCode(callSuper = false)
public class RewardAsset implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "period")
    @TableField( "`period`")
    private String period;

    @ApiModelProperty(value = "rank")
    @TableField( "`rank`")
    private Integer rank;

    @ApiModelProperty(value = "url")
    private String imageUrl;
}
