package com.wikex.wikex.market.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.market.processor.CoinProcessor;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Currency;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.CurrencyFeign;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
@Component
@Slf4j
@ToString
public class CoinExchangeRate {
    @Getter
    @Setter
    private BigDecimal usdCnyRate = new BigDecimal("6.45");

    @Getter
    @Setter
    private BigDecimal usdtCnyRate = new BigDecimal("6.98");

    @Getter
    @Setter
    private BigDecimal usdJpyRate = new BigDecimal("110.02");
    @Getter
    @Setter
    private BigDecimal usdHkdRate = new BigDecimal("7.8491");
    @Getter
    @Setter
    private BigDecimal sgdCnyRate = new BigDecimal("5.08");
    @Setter
    private CoinProcessorFactory coinProcessorFactory;

    private Map<String,BigDecimal> ratesMap = new HashMap<String,BigDecimal>(){{
        put("CNY",new BigDecimal("6.36"));
        put("TWD",new BigDecimal("6.40"));
        put("USD",new BigDecimal("1.00"));
        put("EUR",new BigDecimal("0.91"));
        put("HKD",new BigDecimal("7.81"));
        put("SGD",new BigDecimal("1.36"));
    }};

    @Autowired
    private CoinFeign coinService;
    @Autowired
    private ExchangeCoinFeign exCoinService;

    @Autowired
    private CurrencyFeign currencyFeign;


    public BigDecimal getUsdRate(String symbol) {
        
        if ("USDT".equalsIgnoreCase(symbol)) {
            
            return BigDecimal.ONE;
        } else if ("CNY".equalsIgnoreCase(symbol)) {
            
            BigDecimal bigDecimal = BigDecimal.ONE.divide(usdtCnyRate, 4,BigDecimal.ROUND_DOWN).setScale(4, BigDecimal.ROUND_DOWN);
            return bigDecimal;
        }else if ("BITCNY".equalsIgnoreCase(symbol)) {
            BigDecimal bigDecimal = BigDecimal.ONE.divide(usdCnyRate, 4,BigDecimal.ROUND_DOWN).setScale(4, BigDecimal.ROUND_DOWN);
            return bigDecimal;
        } else if ("ET".equalsIgnoreCase(symbol)) {
            BigDecimal bigDecimal = BigDecimal.ONE.divide(usdCnyRate, 4,BigDecimal.ROUND_DOWN).setScale(4, BigDecimal.ROUND_DOWN);
            return bigDecimal;
        } else if ("JPY".equalsIgnoreCase(symbol)) {
            BigDecimal bigDecimal = BigDecimal.ONE.divide(usdJpyRate, 4,BigDecimal.ROUND_DOWN).setScale(4, BigDecimal.ROUND_DOWN);
            return bigDecimal;
        }else if ("HKD".equalsIgnoreCase(symbol)) {
            BigDecimal bigDecimal = BigDecimal.ONE.divide(usdHkdRate, 4,BigDecimal.ROUND_DOWN).setScale(4, BigDecimal.ROUND_DOWN);
            return bigDecimal;
        }
        String usdtSymbol = symbol.toUpperCase() + "/USDT";
        String btcSymbol = symbol.toUpperCase() + "/BTC";
        String ethSymbol = symbol.toUpperCase() + "/ETH";

        if (coinProcessorFactory != null) {
            if (coinProcessorFactory.containsProcessor(usdtSymbol)) {
                
                CoinProcessor processor = coinProcessorFactory.getProcessor(usdtSymbol);
                if(processor == null) {
                	return BigDecimal.ZERO;
                }
                CoinThumb thumb = processor.getThumb();
                if(thumb == null) {
                	
                	return BigDecimal.ZERO;
                }
                return thumb.getUsdRate();
            } else if (coinProcessorFactory.containsProcessor(btcSymbol)) {
                
                CoinProcessor processor = coinProcessorFactory.getProcessor(btcSymbol);
                if(processor == null) {
                	return BigDecimal.ZERO;
                }
                CoinThumb thumb = processor.getThumb();
                if(thumb == null) {
                	
                	return BigDecimal.ZERO;
                }
                return thumb.getUsdRate();
            } else if (coinProcessorFactory.containsProcessor(ethSymbol)) {
                
                CoinProcessor processor = coinProcessorFactory.getProcessor(ethSymbol);
                if(processor == null) {
                	return BigDecimal.ZERO;
                }
                CoinThumb thumb = processor.getThumb();
                if(thumb == null) {
                	
                	return BigDecimal.ZERO;
                }
                return thumb.getUsdRate();
            } else {
                return getDefaultUsdRate(symbol);
            }
        } else {
            return getDefaultUsdRate(symbol);
        }
    }

    /**
     * 
     *
     * @param symbol
     * @return
     */
    public BigDecimal getDefaultUsdRate(String symbol) {
        Coin coin = coinService.findByUnit(symbol);
        if (coin != null) {
            return coin.getUsdRate();
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCnyRate(String symbol) {
        if ("CNY".equalsIgnoreCase(symbol)) {
            return BigDecimal.ONE;
        } else if("ET".equalsIgnoreCase(symbol)){
            return BigDecimal.ONE;
        }
        return getUsdRate(symbol).multiply(usdtCnyRate).setScale(2, RoundingMode.DOWN);
    }

    public BigDecimal getJpyRate(String symbol) {
        if ("JPY".equalsIgnoreCase(symbol)) {
            return BigDecimal.ONE;
        }
        return getUsdRate(symbol).multiply(usdJpyRate).setScale(2, RoundingMode.DOWN);
    }

    public BigDecimal getHkdRate(String symbol) {
        if ("HKD".equalsIgnoreCase(symbol)) {
            return BigDecimal.ONE;
        }
        return getUsdRate(symbol).multiply(usdHkdRate).setScale(2, RoundingMode.DOWN);
    }

    /**
     * 
     *
     * @throws UnirestException
     */

//    @Scheduled(cron = "0 */15 * * * *")
    @XxlJob("syncCurrencyMap")
    public void syncCurrencyMap() {
        try {
            List<Currency> allCurrency = currencyFeign.findAllCurrency();
            if (allCurrency != null) {
                for (Currency currency : allCurrency) {
                    ratesMap.put(currency.getFullName(),currency.getRate());
                    if("CNY".equals(currency.getFullName().toUpperCase())){
                        setUsdCnyRate(currency.getRate());
                        setUsdtCnyRate(currency.getRate());
                    }else if("JPY".equals(currency.getFullName().toUpperCase())){
                        setUsdJpyRate(currency.getRate());
                    }else if("HKD".equals(currency.getFullName().toUpperCase())){
                        setUsdHkdRate(currency.getRate());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to sync currency map from Feign service: {}", e.getMessage(), e);
        }


//            String urlOk="https://www.okex.com/v3/c2c/otc-ticker?&baseCurrency=USDT&quoteCurrency=CNY";
//            try {
//                HttpResponse<JsonNode> resp = Unirest.get(urlOk)
//                        .connectTimeout(5000)
//                        .socketTimeout(10000)
//                        .asJson();
//                if(resp.getStatus() == 200) {
//                    JSONObject ret = JSON.parseObject(resp.getBody().toString());
//                    if(ret.getIntValue("code") == 0) {
//                        double doubleValue = ret.getJSONObject("data").getDoubleValue("otcTicker");
//                        setUsdtCnyRate(new BigDecimal(doubleValue).setScale(2, RoundingMode.HALF_UP));
//                    }
//                }
//            } catch (UnirestException e) {
//                log.error("Failed to fetch exchange rate from OKEx: {}", e.getMessage(), e);
//            } catch (Exception e) {
//                log.error("Unexpected error while fetching exchange rate: {}", e.getMessage(), e);
//            }

    }


    public HashMap<String, BigDecimal> getAllRate(String symbol) {
        HashMap<String,BigDecimal> result = new HashMap<>();
        Set<String> keySet = ratesMap.keySet();
        for (String currency : keySet) {
            if ("CNY".equalsIgnoreCase(symbol)) {
                result.put(currency,BigDecimal.ONE);
                continue;
            } else if("ET".equalsIgnoreCase(symbol)){
                result.put(currency,BigDecimal.ONE);
                continue;
            }
            BigDecimal usdtRate = ratesMap.get(currency);
            BigDecimal rate = getUsdRate(symbol).multiply(usdtRate).setScale(2, RoundingMode.DOWN);
            result.put(currency,rate);
        }
        return result;
    }
}
