package com.wikex.wikex.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Trading favorite symbol
 * </p>
 *
 * Represents a trading pair that a user has marked as a favorite for quick access or priority.
 *
 */
@ApiModel(value = "Trading favorite symbol")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExchangeFavorSymbol implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Added time")
    private String addTime;

    @ApiModelProperty(value = "User ID")
    private Long memberId;

    @ApiModelProperty(value = "Trading pair symbol")
    private String symbol;

}
