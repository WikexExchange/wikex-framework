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
 * Member Second Contract Wallet
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Second Contract Wallet")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberSecondWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Coin Balance
     */
    @ApiModelProperty(value = "Coin Balance")
    private BigDecimal balance;

    /**
     * Frozen Balance
     */
    @ApiModelProperty(value = "Frozen Balance")
    private BigDecimal frozenBalance;

    /**
     * Member ID
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Coin ID
     */
    @ApiModelProperty(value = "Coin ID")
    private String coinId;

}
