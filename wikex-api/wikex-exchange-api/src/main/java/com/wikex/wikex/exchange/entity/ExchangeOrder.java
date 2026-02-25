package com.wikex.wikex.exchange.entity;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderStatus;
import com.wikex.wikex.constant.ExchangeOrderType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Spot trading order
 * </p>
 *
 * Represents a limit or market order placed in the spot trading market.
 * Includes information about order type, amount, price, status, and execution details.
 * 
 */
@ApiModel(value = "Spot trading order")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExchangeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Order ID")
    @TableId("order_id")
    private String orderId;

    @ApiModelProperty(value = "Amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "Base currency / Settlement currency")
    private String baseSymbol;

    @ApiModelProperty(value = "Canceled time")
    private Long canceledTime;

    @ApiModelProperty(value = "Coin symbol")
    private String coinSymbol;

    @ApiModelProperty(value = "Completed time")
    private Long completedTime;

    @ApiModelProperty(value = "Order direction")
    private ExchangeOrderDirection direction;

    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    @ApiModelProperty(value = "Price")
    private BigDecimal price;

    @ApiModelProperty(value = "Order status")
    private ExchangeOrderStatus status;

    @ApiModelProperty(value = "Trading pair symbol")
    private String symbol;

    @ApiModelProperty(value = "Order creation time")
    private Long time;

    @ApiModelProperty(value = "Traded amount")
    private BigDecimal tradedAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "Turnover")
    private BigDecimal turnover = BigDecimal.ZERO;

    @ApiModelProperty(value = "Order type")
    private ExchangeOrderType type;

    @ApiModelProperty(value = "Use discount (e.g., 0)")
    private String useDiscount;

    @ApiModelProperty(value = "Order source")
    private Integer orderResource;

    @TableField(exist = false)
    private List<ExchangeOrderDetail> detail;

    @TableField(exist = false)
    private BigDecimal fee;

}
