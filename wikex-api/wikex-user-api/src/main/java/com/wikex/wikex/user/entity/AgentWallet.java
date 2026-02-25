package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;


@ApiModel(value = "Agent Wallet")
@Data
@EqualsAndHashCode(callSuper = false)
public class AgentWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "user id")
    private Long memberId;

    @ApiModelProperty(value = "coin unit")
    private String coinUnit;

    /**
     * Available balance
     */
    @ApiModelProperty(value = "available balance")
    private BigDecimal balance;

    @ApiModelProperty(value = "update time")
    private Long updateTime;

    @ApiModelProperty(value = "creation time")
    private Long createTime;
}
