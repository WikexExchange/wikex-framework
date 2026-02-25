package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.wikex.wikex.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Member Wallet
 * </p>
 * 
 */
@ApiModel(value = "Member Wallet")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Available balance
     */
    @ApiModelProperty(value = "Available Balance")
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Frozen balance
     */
    @ApiModelProperty(value = "Frozen Balance")
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    /**
     * Balance pending release
     */
    @ApiModelProperty(value = "Balance Pending Release")
    private BigDecimal releaseBalance = BigDecimal.ZERO;

    /**
     * Wallet lock status
     */
    @ApiModelProperty(value = "Wallet Lock Status")
    private BooleanEnum isLock = BooleanEnum.IS_FALSE;

    /**
     * User ID
     */
    @ApiModelProperty(value = "User ID")
    private Long memberId;

    /**
     * Version number for optimistic locking
     */
    @ApiModelProperty(value = "Version Number")
    @Version
    private Integer version;

    /**
     * Coin ID
     */
    @ApiModelProperty(value = "Coin ID")
    private String coinId;

}
