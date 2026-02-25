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
 * Withdraw request
 * </p>
 *
 * @author markchao
 * @since 2022-04-06
 */
@ApiModel(value = "Withdraw request")
@Data
@EqualsAndHashCode(callSuper = false)
public class Withdraw implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * User
     */
    @ApiModelProperty(value = "User")
    private Integer memberId;

    @ApiModelProperty(value = "Add time")
    private Long addTime;

    /**
     * Coin ID
     */
    @ApiModelProperty(value = "Coin ID")
    private Integer coinId;

    /**
     * Coin name
     */
    @ApiModelProperty(value = "Coin name")
    private String coinName;

    /**
     * Withdraw address
     */
    @ApiModelProperty(value = "Withdraw address")
    private String address;

    /**
     * Requested amount
     */
    @ApiModelProperty(value = "Requested amount")
    private BigDecimal money;

    /**
     * Withdraw fee
     */
    @ApiModelProperty(value = "Withdraw fee")
    private BigDecimal fee;

    /**
     * Actual amount received
     */
    @ApiModelProperty(value = "Actual amount received")
    private BigDecimal realMoney;

    /**
     * Processing mode, 0 blockchain processing, 1 external processing
     */
    @ApiModelProperty(value = "Processing mode, 0 blockchain processing, 1 external processing")
    private Integer processMold;

    /**
     * Withdraw hash
     */
    @ApiModelProperty(value = "Withdraw hash")
    private String hash;

    /**
     * Status: -1 rejected, 0 pending, 1 processing, 2 processed, 3 failed
     */
    @ApiModelProperty(value = "Status: -1 rejected, 0 pending, 1 processing, 2 processed, 3 failed")
    private Integer status;

    /**
     * Backend processing time
     */
    @ApiModelProperty(value = "Backend processing time")
    private Long processTime;

    /**
     * Withdraw failure reason (can be filled for backend rejection reason)
     */
    @ApiModelProperty(value = "Withdraw failure reason")
    private String withdrawInfo;

    /**
     * User withdrawal remark
     */
    @ApiModelProperty(value = "User withdrawal remark")
    private String remark;

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
