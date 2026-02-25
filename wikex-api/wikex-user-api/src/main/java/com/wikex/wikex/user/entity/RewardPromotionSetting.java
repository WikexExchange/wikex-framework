package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.wikex.wikex.constant.PromotionRewardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Promotion reward settings
 * </p>
 *
 * @author markchao
 * @since 2021-08-04
 */
@ApiModel(value = "Promotion reward settings")
@Data
@EqualsAndHashCode(callSuper = false)
public class RewardPromotionSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Effective time, counted from registration date in days
     */
    @ApiModelProperty(value = "Effective time, counted from registration date in days")
    private Integer effectiveTime;

    @ApiModelProperty(value = "Information")
    private String info;

    /**
     * Status: 0 disabled, 1 enabled
     */
    @ApiModelProperty(value = "Status: 0 disabled, 1 enabled")
    private Integer status;

    /**
     * 0: Promotion registration, 1: Fiat promotion transaction, 2: Crypto trading
     */
    @ApiModelProperty(value = "0: Promotion registration, 1: Fiat promotion transaction, 2: Crypto trading")
    private PromotionRewardType type;

    @ApiModelProperty(value = "Update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "Admin id")
    private Long adminId;

    /**
     * Reward coin
     */
    @ApiModelProperty(value = "Reward coin")
    private String coinId;

}
