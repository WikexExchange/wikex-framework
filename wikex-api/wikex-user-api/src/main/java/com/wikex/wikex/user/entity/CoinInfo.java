package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

@ApiModel(value = "Coin Info")
@Data
@EqualsAndHashCode(callSuper = false)
public class CoinInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "coin id")
    private Long coinId;

    @ApiModelProperty(value = "coingecko id")
    private String coingeckoId;

    @ApiModelProperty(value = "total supply")
    private BigDecimal totalSupply;

    @ApiModelProperty(value = "max supply")
    private BigDecimal maxSupply;

    @ApiModelProperty(value = "circulating supply")
    private BigDecimal circulatingSupply;

    @ApiModelProperty(value = "market cap usd")
    private BigDecimal marketCapUsd;

    @ApiModelProperty(value = "fully diluted valuation usd")
    private BigDecimal fdvUsd;

    @ApiModelProperty(value = "description")
    private String description;
}
