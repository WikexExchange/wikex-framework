package com.wikex.wikex.exchange.util;

import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderStatus;
import com.wikex.wikex.constant.ExchangeOrderType;
import com.wikex.wikex.exchange.entity.ExchangeOrder;

public class OrderUtils {

    public static boolean isCompleted(ExchangeOrder order){
        if(order.getStatus() != ExchangeOrderStatus.TRADING) {
            return true;
        } else{
            if(order.getType() == ExchangeOrderType.MARKET_PRICE && order.getDirection() == ExchangeOrderDirection.BUY){
                return order.getAmount().compareTo(order.getTurnover()) <= 0;
            }
            else{
                return order.getAmount().compareTo(order.getTradedAmount()) <= 0;
            }
        }
    }
}
