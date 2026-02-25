package com.wikex.wikex.second.handler;

import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ContractTrade;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.second.job.ExchangePushJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public void handleTrades(String symbol, List<ContractTrade> contractTrades, CoinThumb thumb) {

    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        messagingTemplate.convertAndSend("/topic/second/kline/"+symbol,kLine);
    }
}
