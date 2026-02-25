package com.wikex.wikex.market.handler;

import com.wikex.wikex.market.job.ExchangePushJob;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ExchangeTrade;
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

    /**
     * @param symbol
     * @param exchangeTrade
     * @param thumb
     */
    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        try {
            pushJob.addThumb(symbol, thumb);
        } catch (Exception e) {
            // TODO
        }
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        try {
            messagingTemplate.convertAndSend("/topic/market/kline/" + symbol, kLine);
        } catch (Exception e) {
            // TODO
        }
    }
}
