package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Member Invitation Record
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Invitation Record")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberInviteStastic implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Total BTC rewards
     */
    @ApiModelProperty(value = "Total BTC rewards")
    private BigDecimal btcReward;

    /**
     * Estimated total rewards (in USDT equivalent)
     */
    @ApiModelProperty(value = "Estimated total rewards (USDT equivalent)")
    private BigDecimal estimatedReward;

    /**
     * Total ETH rewards
     */
    @ApiModelProperty(value = "Total ETH rewards")
    private BigDecimal ethReward;

    /**
     * Extra rewards (USDT equivalent)
     */
    @ApiModelProperty(value = "Extra rewards (USDT equivalent)")
    private BigDecimal extraReward;

    /**
     * Is robot (0: no, 1: yes)
     */
    @ApiModelProperty(value = "Is robot (0: no, 1: yes)")
    private Integer isRobot;

    /**
     * Number of level 1 invited friends
     */
    @ApiModelProperty(value = "Number of level 1 invited friends")
    private Integer levelOne;

    /**
     * Number of level 2 invited friends
     */
    @ApiModelProperty(value = "Number of level 2 invited friends")
    private Integer levelTwo;

    /**
     * Member ID
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Other rewards (JSON format, e.g. {"LTC":123, "EOS":22.3})
     */
    @ApiModelProperty(value = "Other rewards (JSON format)")
    private String otherReward;

    /**
     * Statistics date
     */
    @ApiModelProperty(value = "Statistics date")
    private String stasticDate;

    /**
     * Total USDT rewards
     */
    @ApiModelProperty(value = "Total USDT rewards")
    private BigDecimal usdtReward;

    /**
     * User identifier (phone or email)
     */
    @ApiModelProperty(value = "User identifier (phone or email)")
    private String userIdentify;

}
