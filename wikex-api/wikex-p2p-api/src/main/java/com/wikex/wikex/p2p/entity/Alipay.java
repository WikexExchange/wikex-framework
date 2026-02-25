package com.wikex.wikex.p2p.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Alipay Payment Information
 * Stores account and QR code details for Alipay transactions in P2P trading.
 * 
 */
@ApiModel(value = "Alipay Information")
@Data
public class Alipay implements Serializable {
    private static final long serialVersionUID = 8317734763036284945L;
    /**
     * Alipay account number or ID.
     */
    @ApiModelProperty(value = "Alipay account number/ID")
    private String aliNo;
    /**
     * URL of the Alipay payment QR code image.
     */
    @ApiModelProperty(value = "Alipay payment QR code URL")
    private String qrCodeUrl;
}
