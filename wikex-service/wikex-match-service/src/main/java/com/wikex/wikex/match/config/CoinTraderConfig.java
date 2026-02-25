package com.wikex.wikex.match.config;

import com.wikex.wikex.constant.ExchangeCoinPublishType;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.match.service.ExchangeCoinService;

import com.wikex.wikex.match.trader.CoinTrader;
import com.wikex.wikex.match.trader.CoinTraderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class CoinTraderConfig {


    @Autowired
    private TradingConfig tradingConfig;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * @param exchangeCoinService
     * @return
     */
    @Bean
    public CoinTraderFactory getCoinTrader(ExchangeCoinService exchangeCoinService, RocketMQTemplate rocketMQTemplate){
        CoinTraderFactory factory = new CoinTraderFactory();
        List<ExchangeCoin> coins = this.getExchangeCoins(exchangeCoinService);
        for(ExchangeCoin coin:coins) {
            
            CoinTrader trader = new CoinTrader(coin.getSymbol());
            trader.setRocketMQTemplate(rocketMQTemplate);
            trader.setBaseCoinScale(coin.getBaseCoinScale());
            trader.setCoinScale(coin.getCoinScale());
            trader.setPublishType(ExchangeCoinPublishType.creator(coin.getPublishType()));
            trader.setClearTime(coin.getClearTime());
            
            // Set price threshold from ExchangeCoin config
            // If not configured (null or <= 0), use default values (0.8 for BID, 1.2 for ASK)
            BigDecimal bidThreshold = coin.getBidPriceThreshold();
            BigDecimal askThreshold = coin.getAskPriceThreshold();
            trader.setPriceThreshold(bidThreshold, askThreshold);
            
            trader.stopTrading();
            factory.addTrader(coin.getSymbol(),trader);
        }
        return factory;
    }

    /**
     * @param exchangeCoinService
     * @return
     */
    private List<ExchangeCoin> getExchangeCoins(ExchangeCoinService exchangeCoinService){

        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        List<ExchangeCoin> result = new ArrayList<>();
        Map<String, List<String>> pairsMap = tradingConfig.getPairs();
        if (pairsMap!=null) {
            List<String> pairs = pairsMap.get(applicationName);
            if(pairs!=null && pairs.size()>0){
                for(ExchangeCoin coin:coins){
                    if(pairs.contains(coin.getSymbol())){
                        result.add(coin);
                    }
                }
            }else {
                Collection<List<String>> values = pairsMap.values();
                for(ExchangeCoin coin:coins){
                    boolean flag = false;
                    for(List<String> list:values){
                        if(list.contains(coin.getSymbol())){
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        result.add(coin);
                    }
                }

            }
        }else {
            result = coins;
        }
        return result;
    }
}
