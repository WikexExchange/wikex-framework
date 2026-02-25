package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.constant.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * OTC Order
 * </p>
 */
@ApiModel(value = "OTC Order")
@Data
@EqualsAndHashCode(callSuper = false)
public class OtcOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Advertisement ID
     */
    @ApiModelProperty(value = "Advertisement ID")
    private Long advertiseId;

    /**
     * Advertisement type: 0 for buy, 1 for sell
     */
    @ApiModelProperty(value = "Advertisement type: 0 for buy, 1 for sell")
    private AdvertiseType advertiseType;

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
     * Cancellation time
     */
    @ApiModelProperty(value = "Cancellation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;

    /**
     * Transaction fee
     */
    @ApiModelProperty(value = "Transaction fee")
    private BigDecimal commission;

    /**
     * Country
     */
    @ApiModelProperty(value = "Country")
    private String country;

    /**
     * Transaction creation time
     */
    @ApiModelProperty(value = "Transaction creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Trader ID
     */
    @ApiModelProperty(value = "Trader ID")
    private Long customerId;

    /**
     * Trader username
     */
    @ApiModelProperty(value = "Trader username")
    private String customerName;

    /**
     * Trader real name
     */
    @ApiModelProperty(value = "Trader real name")
    private String customerRealName;

    /**
     * Maximum transaction limit
     */
    @ApiModelProperty(value = "Maximum transaction limit")
    private BigDecimal maxLimit;

    /**
     * Advertiser ID
     */
    @ApiModelProperty(value = "Advertiser ID")
    private Long memberId;

    /**
     * Advertiser username
     */
    @ApiModelProperty(value = "Advertiser username")
    private String memberName;

    /**
     * Advertiser real name
     */
    @ApiModelProperty(value = "Advertiser real name")
    private String memberRealName;

    /**
     * Minimum transaction limit
     */
    @ApiModelProperty(value = "Minimum transaction limit")
    private BigDecimal minLimit;

    /**
     * Transaction amount
     */
    @ApiModelProperty(value = "Transaction amount")
    private BigDecimal money;

    /**
     * Transaction quantity
     */
    @ApiModelProperty(value = "Transaction quantity")
    private BigDecimal number;

    /**
     * Order number
     */
    @ApiModelProperty(value = "Order number")
    private String orderSn;

    /**
     * Payment method (only one can be selected: alipay, bank, wechatpay), default is bank
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
     * Release time
     */
    @ApiModelProperty(value = "Release time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date releaseTime;

    /**
     * Customer remarks
     */
    @ApiModelProperty(value = "Customer remarks")
    private String remark;

    /**
     * Status: 0 canceled, 1 unpaid, 2 paid, 3 completed, 4 under appeal
     */
    @ApiModelProperty(value = "Status: 0 canceled, 1 unpaid, 2 paid, 3 completed, 4 under appeal")
    private OrderStatus status;

    /**
     * Payment deadline (in minutes)
     */
    @ApiModelProperty(value = "Payment deadline (in minutes)")
    private Integer timeLimit;

    /**
     * Version
     */
    @ApiModelProperty(value = "Version")
    private Long version;

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
     * Coin ID
     */
    @ApiModelProperty(value = "Coin ID")
    private Long coinId;

    @TableField(exist = false)
    private Alipay alipay;
    @TableField(exist = false)
    private BankInfo bankInfo;
    @TableField(exist = false)
    private WechatPay wechatPay;

}
