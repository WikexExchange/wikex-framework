package com.wikex.wikex.option.handler;

import com.wikex.wikex.option.job.ExchangePushJob;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.KLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebsocketMarketHandler implements MarketHandler{
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ExchangePushJob pushJob;

    
    @Override
    public void handleTrade(String symbol, CoinThumb thumb) {
        
        pushJob.addThumb(symbol,thumb);
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        
        messagingTemplate.convertAndSend("/topic/option/kline/"+symbol,kLine);
    }
}
