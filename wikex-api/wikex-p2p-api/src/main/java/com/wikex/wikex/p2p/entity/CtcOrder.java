package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * CTC Order
 * </p>
 *
 */
@ApiModel(value = "CTC Order")
@Data
@EqualsAndHashCode(callSuper = false)
public class CtcOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Alipay account
     */
    @ApiModelProperty(value = "Alipay account")
    private String aliNo;

    /**
     * Alipay payment QR code
     */
    @ApiModelProperty(value = "Alipay payment QR code")
    private String qrCodeUrl;

    /**
     * Buy/sell quantity
     */
    @ApiModelProperty(value = "Buy/sell quantity")
    private BigDecimal amount;

    /**
     * Bank
     */
    @ApiModelProperty(value = "Bank")
    private String bank;

    /**
     * Bank branch
     */
    @ApiModelProperty(value = "Bank branch")
    private String branch;

    /**
     * Bank card number
     */
    @ApiModelProperty(value = "Bank card number")
    private String cardNo;

    /**
     * Cancel reason
     */
    @ApiModelProperty(value = "Cancel reason")
    private String cancelReason;

    /**
     * Cancel time
     */
    @ApiModelProperty(value = "Cancel time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;

    /**
     * Completion time
     */
    @ApiModelProperty(value = "Completion time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;

    /**
     * Order acceptance/confirmation time (When the user buys in, the confirmation time is the same as the creation time)
     */
    @ApiModelProperty(value = "Order acceptance/confirmation time (When the user buys in, the confirmation time is the same as the creation time)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmTime;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Order type 0: Buy-in  1: Sell-out
     */
    @ApiModelProperty(value = "Order type 0: Buy-in  1: Sell-out")
    private Integer direction;

    /**
     * Transaction amount
     */
    @ApiModelProperty(value = "Transaction amount")
    private BigDecimal money;

    /**
     * Order number
     */
    @ApiModelProperty(value = "Order number")
    private String orderSn;

    /**
     * Payment method (can only choose one: alipay, bank, wechatpay), default is bank
     */
    @ApiModelProperty(value = "Payment method")
    private String payMode;

    /**
     * Payment time
     */
    @ApiModelProperty(value = "Payment time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    /**
     * Price
     */
    @ApiModelProperty(value = "Price")
    private BigDecimal price;

    /**
     * Remark
     */
    @ApiModelProperty(value = "Remark")
    private String remark;

    /**
     * * Status (0: Not accepted  1: Accepted  2: Paid  3: Completed  4: Cancelled)
     * When buying in: automatically changes to Accepted, after the user makes payment, they set status to Paid,
     * the acceptor reviews, if approved, coins are released and status changes to Completed, otherwise to Cancelled.
     * When selling: automatically changes to Not accepted, the acceptor accepts the order and sets status to Accepted,
     * after making payment, sets to Paid & Completed.
     * If the order is in Not accepted status and more than 30 minutes have passed since creation, the order is automatically cancelled.
     * If the order is in Accepted status and more than 30 minutes have passed since creation, the order is automatically cancelled.
     * Only orders in Not accepted status can be cancelled by the user; for other statuses, only the platform or the acceptor can cancel.
     */
    @ApiModelProperty(value = "Status (0: Not accepted  1: Accepted  2: Paid  3: Completed  4: Cancelled)")
    private Integer status;

    /**
     * Currency unit
     */
    @ApiModelProperty(value = "Currency unit")
    private String unit;

    /**
     * WeChat payment QR code
     */
    @ApiModelProperty(value = "WeChat payment QR code")
    private String qrWeCodeUrl;

    /**
     * WeChat ID
     */
    @ApiModelProperty(value = "WeChat ID")
    private String wechat;

    /**
     * Acceptor user ID
     */
    @ApiModelProperty(value = "Acceptor user ID")
    private Long acceptorId;

    /**
     * Order initiator user ID
     */
    @ApiModelProperty(value = "Order initiator user ID")
    private Long memberId;

    /**
     * When the user buys in, this is the acceptor's real name.
     * When the user sells, this is the user's real name.
     */
    @ApiModelProperty(value = "When buying in, this is the acceptor's real name; when selling, this is the user's real name")
    private String realName;

}
