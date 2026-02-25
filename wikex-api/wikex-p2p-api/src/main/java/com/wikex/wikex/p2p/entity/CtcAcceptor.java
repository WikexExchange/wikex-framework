package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CTC Merchant (USDT)
 * Represents a merchant in the CTC system dealing in USDT with CNY exchange.
 *
 * Author: markchao
 * Since: 2021-08-21
 */
@ApiModel(value = "CTC Merchant (USDT)")
@Data
@EqualsAndHashCode(callSuper = false)
public class CtcAcceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Primary key ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Merchant status (0=Inactive, 1=Active)")
    private Integer status;

    @ApiModelProperty(value = "User ID of the merchant")
    private Long memberId;

    @ApiModelProperty(value = "CNY amount bought by merchant")
    private BigDecimal cnyIn;

    @ApiModelProperty(value = "CNY amount sold by merchant")
    private BigDecimal cnyOut;

    @ApiModelProperty(value = "USDT amount bought by merchant")
    private BigDecimal usdtIn;

    @ApiModelProperty(value = "USDT amount sold by merchant")
    private BigDecimal usdtOut;


}
