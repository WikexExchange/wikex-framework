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
 * Member Funding Wallet
 */
@ApiModel(value = "Member Funding Wallet")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberFundingWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Balance")
    private BigDecimal balance = BigDecimal.ZERO;

    @ApiModelProperty(value = "Frozen Balance")
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    @ApiModelProperty(value = "Coin ID")
    private String coinId;
}
