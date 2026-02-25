package com.wikex.wikex.p2p.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@ApiModel(value = "Buy and Sell Details")
@Builder
@Data
public class PreOrderInfo {
    private String username;
    private int emailVerified;
    private int phoneVerified;
    private int idCardVerified;
    private int transactions;
    private long otcCoinId;
    private String unit;
    private BigDecimal price;
    private BigDecimal number;
    private String payMode;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private int timeLimit;
    private String country;
    private String currency;
    private int advertiseType;
    private String remark;

    // Maximum tradable amount
    @ApiModelProperty(value = "Maximum tradable amount")
    private BigDecimal maxTradableAmount;
}
