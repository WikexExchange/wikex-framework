package com.wikex.wikex.swap.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.ContractRewardRecordType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Perpetual Contract Reward Record
 * </p>
 *
 * @author sulinxin
 * @since 2021-08-23
 */
@ApiModel(value = "Perpetual Contract Reward Record")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractRewardRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
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
     * 0 - Open position commission
     * 1 - Close position commission
     * 2 - Peer level reward
     * 3 - Platform fee income
     */
    @ApiModelProperty(value = "Reward type: 0 - Open position commission, 1 - Close position commission, 2 - Peer level reward, 3 - Platform fee income")
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
