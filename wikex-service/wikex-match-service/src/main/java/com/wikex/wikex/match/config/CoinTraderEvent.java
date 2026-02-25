package com.wikex.wikex.match.config;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.match.service.ExchangeOrderDetailService;
import com.wikex.wikex.match.service.ExchangeOrderService;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.util.OrderUtils;
import com.wikex.wikex.match.trader.CoinTrader;
import com.wikex.wikex.match.trader.CoinTraderFactory;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CoinTraderEvent implements ApplicationRunner {
    private Logger log = LoggerFactory.getLogger(CoinTraderEvent.class);
    @Autowired
    CoinTraderFactory coinTraderFactory;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        // coinTraderFactory.getTraderMap();
        Map<String, CoinTrader> traders = coinTraderFactory.getTraderMap();
        traders.forEach((symbol,trader) ->{
            
            List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
            
            List<ExchangeOrder> tradingOrders = new ArrayList<>();
            List<ExchangeOrder> completedOrders = new ArrayList<>();
            orders.forEach(order -> {
                BigDecimal tradedAmount = BigDecimal.ZERO;
                BigDecimal turnover = BigDecimal.ZERO;
                List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());

                for(ExchangeOrderDetail trade:details){
                    tradedAmount = tradedAmount.add(trade.getAmount());
                    turnover = turnover.add(trade.getAmount().multiply(trade.getPrice()));
                }
                order.setTradedAmount(tradedAmount);
                order.setTurnover(turnover);
                if(!OrderUtils.isCompleted(order)){
                    tradingOrders.add(order);
                }
                else{
                    completedOrders.add(order);
                }
            });
            
            try {
                trader.trade(tradingOrders);
            } catch (ParseException e) {
                log.error("Error rebuilding orderbook on startup, symbol={}", symbol, e);
            }
            if(completedOrders.size() > 0){
                
                rocketMQTemplate.convertAndSend("exchange-order-completed", JSON.toJSONString(completedOrders));
            }
            trader.setReady(true);
        });
    }
}
