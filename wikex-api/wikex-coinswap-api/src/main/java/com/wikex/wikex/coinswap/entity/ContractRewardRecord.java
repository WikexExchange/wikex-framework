package com.wikex.wikex.coinswap.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.ContractRewardRecordType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Perpetual Contract Reward Record
 * </p>
 *
 * Author: sulinxin
 * Since: 2021-08-23
 */
@ApiModel(value = "Perpetual Contract Reward Record")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractRewardRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    private Date createTime;

    /**
     * Reward amount
     */
    @ApiModelProperty(value = "Reward amount")
    private BigDecimal num;

    /**
     * Reward type:
     * 0 - Commission rebate on opening
     * 1 - Commission rebate on closing
     * 2 - Same-level reward
     * 3 - Platform fee income
     */
    @ApiModelProperty(value = "0 - Commission rebate on opening, 1 - Commission rebate on closing, 2 - Same-level reward, 3 - Platform fee income")
    private ContractRewardRecordType type;

    /**
     * Coin ID
     */
    @ApiModelProperty(value = "Coin ID")
    private String coinId;

    /**
     * Entrust order ID
     */
    @ApiModelProperty(value = "Entrust order ID")
    private Long orderId;

    /**
     * Source user ID
     */
    @ApiModelProperty(value = "Source user ID")
    private Long fromMemberId;

    /**
     * User ID
     */
    @ApiModelProperty(value = "User ID")
    private Long memberId;

}
