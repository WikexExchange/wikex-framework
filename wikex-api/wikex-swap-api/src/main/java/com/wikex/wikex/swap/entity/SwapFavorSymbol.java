package com.wikex.wikex.swap.entity;

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
 * @author markchao
 * @since 2022-02-07
 */
@ApiModel(value = "Trading Favorite Symbol")
@Data
@EqualsAndHashCode(callSuper = false)
public class SwapFavorSymbol implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "time added")
    private String addTime;

    @ApiModelProperty(value = "user id")
    private Long memberId;

    @ApiModelProperty(value = "trading pair symbol")
    private String symbol;

}
