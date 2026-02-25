package com.wikex.wikex.screen;

import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderStatus;
import com.wikex.wikex.constant.ExchangeOrderType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeOrderScreen extends PageParam{
    ExchangeOrderType type;
    String coinSymbol;
    String baseSymbol ;
    ExchangeOrderStatus status; 
    Long memberId;
    
    BigDecimal minPrice ;
    BigDecimal maxPrice ;
    
    BigDecimal minTradeAmount;
    BigDecimal maxTradeAmount;
    
    BigDecimal minTurnOver;
    BigDecimal maxTurnOver;
    String orderId ;
    ExchangeOrderDirection orderDirection ;
    
    Integer robotOrder;

    
    BooleanEnum completed ;
}
