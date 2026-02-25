package com.wikex.wikex.user.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Recharge
 * </p>
 *
 * @author markchao
 * @since 2022-03-20
 */
@ApiModel(value = "Recharge")
@Data
@EqualsAndHashCode(callSuper = false)
public class Recharge implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Hash
     */
    @ApiModelProperty(value = "Hash")
    private String hash;

    /**
     * Hash MD5
     */
    @ApiModelProperty(value = "Hash MD5")
    private String md5;

    /**
     * User
     */
    @ApiModelProperty(value = "User")
    private Long memberId;

    @ApiModelProperty(value = "Add time")
    private Long addTime;

    /**
     * Coin id
     */
    @ApiModelProperty(value = "Coin id")
    private Integer coinId;

    /**
     * Coin name
     */
    @ApiModelProperty(value = "Coin name")
    private String coinName;

    /**
     * Amount
     */
    @ApiModelProperty(value = "Amount")
    private BigDecimal money;

    /**
     * Block
     */
    @ApiModelProperty(value = "Block")
    private Integer block;

    /**
     * 0 for normal block record, 1 added by backend
     */
    @ApiModelProperty(value = "0 for normal block record, 1 added by backend")
    private Integer agreen;

    /**
     * Number of confirmations
     */
    @ApiModelProperty(value = "Number of confirmations")
    private Integer confirms;

    /**
     * Required number of confirmations
     */
    @ApiModelProperty(value = "Required number of confirmations")
    private Integer nConfirms;

    /**
     * 0: Not received, 1: Received, -1: Failed
     */
    @ApiModelProperty(value = "0: Not received, 1: Received, -1: Failed")
    private Integer status;

    /**
     * Sender address
     */
    @ApiModelProperty(value = "Sender address")
    private String send;

    /**
     * Receiver address
     */
    @ApiModelProperty(value = "Receiver address")
    private String address;

    /**
     * Protocol
     */
    @ApiModelProperty(value = "Protocol")
    private Integer protocol;

    /**
     * Protocol name
     */
    @ApiModelProperty(value = "Protocol name")
    private String protocolName;

}
