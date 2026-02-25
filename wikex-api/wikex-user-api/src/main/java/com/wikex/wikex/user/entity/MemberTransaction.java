package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Member transaction records, including deposits, withdrawals, transfers, etc.
 * </p>
 *
 */
@ApiModel(value = "Member Transaction Record")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    public MemberTransaction() {
        this.createTime = new Date();
    }

    /**
     * Transaction record ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Address involved in transaction
     */
    @ApiModelProperty(value = "Address")
    private String address;

    /**
     * Airdrop ID (if applicable)
     */
    @ApiModelProperty(value = "Airdrop ID")
    private Long airdropId;

    /**
     * Amount deposited or transferred
     */
    @ApiModelProperty(value = "Amount")
    private BigDecimal amount;

    /**
     * Creation time of the transaction record
     */
    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Discount fee applied
     */
    @ApiModelProperty(value = "Discount Fee")
    private String discountFee;

    /**
     * Actual fee charged
     */
    @ApiModelProperty(value = "Actual Fee")
    private String realFee;

    /**
     * Transaction fee
     */
    @ApiModelProperty(value = "Transaction Fee")
    private BigDecimal fee;

    /**
     * Flag to mark specific status or property
     */
    @ApiModelProperty(value = "Flag")
    private Integer flag;

    /**
     * Member ID who performed the transaction
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Coin symbol involved in the transaction
     */
    @ApiModelProperty(value = "Coin Symbol")
    private String symbol;

    /**
     * Transaction type (deposit, withdrawal, transfer, etc.)
     */
    @ApiModelProperty(value = "Transaction Type")
    private Integer type;

    /**
     * Whether commission has been rewarded: 0 = No, 1 = Yes
     */
    @ApiModelProperty(value = "Commission Reward Status")
    private Integer isReward;

    /**
     * Refer Id
     */
    @ApiModelProperty(value = "Refer Id")
    private String referId;

}
