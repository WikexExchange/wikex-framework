package com.wikex.wikex.exchange.entity;

import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.OrderTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * <p>
 * Aggregated Order Details
 * </p>
 *
 * Represents a unified view of order details for both OTC and spot exchange orders,
 * including trade amounts, participants, and related metadata.
 */
@Data
@Document(collection = "order_detail_aggregation")
public class OrderDetailAggregation implements Serializable {

    /**
     * Unique order ID.
     */
    @ApiModelProperty(value = "Order ID")
    private String orderId;

    /**
     * Order type (e.g., OTC or exchange order).
     */
    @ApiModelProperty(value = "Order type (OTC or exchange order)")
    private OrderTypeEnum type;

    /**
     * Username of the order owner.
     */
    @ApiModelProperty(value = "Username")
    private String username;

    /**
     * Real name of the member who owns the order.
     */
    @ApiModelProperty(value = "Member's real name")
    private String realName;

    /**
     * Member's unique ID.
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Timestamp when this aggregated record was generated.
     */
    @ApiModelProperty(value = "Time this aggregation record was created")
    private long time;

    /**
     * Transaction fee charged for this order.
     */
    @ApiModelProperty(value = "Transaction fee")
    private double fee;

    /**
     * Order quantity.
     */
    @ApiModelProperty(value = "Amount")
    private double amount;

    /**
     * Currency unit (e.g., BTC, USDT).
     */
    @ApiModelProperty(value = "Currency unit")
    private String unit;

    /**
     * Order direction (only applicable to exchange orders).
     */
    @ApiModelProperty(value = "Exchange order direction (buy/sell)")
    private ExchangeOrderDirection direction;

    /**
     * Counterparty ID (only applicable to OTC orders).
     */
    @ApiModelProperty(value = "Counterparty ID (OTC order specific)")
    private Long customerId;

    /**
     * Counterparty username (only applicable to OTC orders).
     */
    @ApiModelProperty(value = "Counterparty username (OTC order specific)")
    private String customerName;

    /**
     * Counterparty real name.
     */
    @ApiModelProperty(value = "Counterparty real name")
    private String customerRealName;

}
