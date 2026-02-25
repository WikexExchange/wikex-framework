package com.wikex.wikex.coinswap.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * Trading Favorite Symbol
 * </p>
 *
 * Author: markchao
 * Since: 2022-02-07
 */
@ApiModel(value = "Trading Favorite Symbol")
@Data
@EqualsAndHashCode(callSuper = false)
public class CoinswapFavorSymbol implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Added Time")
    private String addTime;

    @ApiModelProperty(value = "User ID")
    private Long memberId;

    @ApiModelProperty(value = "Trading Pair Symbol")
    private String symbol;
}
