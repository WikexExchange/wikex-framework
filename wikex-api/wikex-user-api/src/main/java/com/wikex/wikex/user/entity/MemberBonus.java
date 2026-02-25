package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * Member Bonus Table
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Bonus Table")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberBonus implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Primary key ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Arrival time of the bonus
     */
    @ApiModelProperty(value = "Arrival time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arriveTime;

    /**
     * Coin identifier
     */
    @ApiModelProperty(value = "Coin ID")
    private String coinId;

    /**
     * Holding time for the coin
     */
    @ApiModelProperty(value = "Holding time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date haveTime;

    /**
     * Bonus amount
     */
    @ApiModelProperty(value = "Bonus amount")
    private BigDecimal memBonus;

    /**
     * Member ID who receives the bonus
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Total fee of the day
     */
    @ApiModelProperty(value = "Total fees for the day")
    private BigDecimal total;

}
