package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * Deposit Record Table
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Deposit Record Table")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberDeposit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Deposit Address
     */
    @ApiModelProperty(value = "Deposit Address")
    private String address;

    /**
     * Amount
     */
    @ApiModelProperty(value = "Amount")
    private BigDecimal amount;

    /**
     * Creation Time
     */
    @ApiModelProperty(value = "Creation Time")
    private LocalDateTime createTime;

    /**
     * Member ID
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Transaction ID
     */
    @ApiModelProperty(value = "Transaction ID")
    private String txid;

    /**
     * Coin Unit (legacy - kept for compatibility)
     */
    @ApiModelProperty(value = "Coin Unit (legacy - kept for compatibility)")
    private String unit;

    /**
     * Asset symbol (preferred — from payload.assetSymbol)
     */
    @ApiModelProperty(value = "Asset symbol (preferred — from payload.assetSymbol)")
    private String assetSymbol;

    /**
     * Deposit status: 0 - DETECTED, 1 - CONFIRMED, 2 - CREDITED
     */
    @ApiModelProperty(value = "Deposit status: 0 - DETECTED, 1 - CONFIRMED, 2 - CREDITED")
    private Integer status;

    /**
     * Number of confirmations observed
     */
    @ApiModelProperty(value = "Number of confirmations observed")
    private Integer confirmations;

    /**
     * From address (sender)
     */
    @ApiModelProperty(value = "From address (sender)")
    private String fromAddress;

    /**
     * Asset contract (token contract address)
     */
    @ApiModelProperty(value = "Asset contract (token contract address)")
    private String assetContract;

    /**
     * Blockchain (e.g. EVM)
     */
    @ApiModelProperty(value = "Blockchain (e.g. EVM)")
    private String blockchain;

    /**
     * Chain key / network key (e.g. bsc)
     */
    @ApiModelProperty(value = "Chain key / network key (e.g. bsc)")
    private String chainKey;

    /**
     * Raw amount string (amountRaw)
     */
    @ApiModelProperty(value = "Raw amount string (amountRaw)")
    private String amountRaw;

    /**
     * Decimals
     */
    @ApiModelProperty(value = "Decimals")
    private Integer decimals;

    @ApiModelProperty(value = "logIndex")
    private Integer logIndex;

    @ApiModelProperty(value = "blockNumber")
    private Long blockNumber;

}
