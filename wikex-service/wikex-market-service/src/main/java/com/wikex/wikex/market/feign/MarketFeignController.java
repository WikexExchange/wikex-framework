package com.wikex.wikex.market.feign;

import com.alibaba.fastjson.JSONArray;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.market.processor.CoinProcessor;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.market.service.MarketService;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.KLine;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("marketFeign")
public class MarketFeignController {
    @Autowired
    private ExchangeCoinFeign exchangeCoinFeign;
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;

    @Autowired
    private MarketService marketService;

    @RequestMapping("engines")
    public Map<String, Integer> engines4Feign() {
        return this.engines();
    }

    /**
     * Get coin summary market data
     * @return
     */
    @RequestMapping("symbolThumb4Feign")
    public List<CoinThumb> findSymbolThumb4Feign(){
        List<ExchangeCoin> coins = exchangeCoinFeign.findAllVisible();
        List<CoinThumb> thumbs = new ArrayList<>();
        for(ExchangeCoin coin:coins){
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            thumb.setZone(coin.getZone());
            thumbs.add(thumb);
        }
        return thumbs;
    }

    private Map<String, Integer> engines() {
        Map<String, CoinProcessor> processorList = coinProcessorFactory.getProcessorMap();
        Map<String, Integer> symbols = new HashMap<String, Integer>();
        processorList.forEach((key, processor) -> {
            if(processor.isStopKline()) {
                symbols.put(key, 2);
            }else {
                symbols.put(key, 1);
            }
        });
        return symbols;
    }

    /**
     * Get coin historical K-line data
     * @param symbol Trading pair symbol
     * @param from Start time
     * @param to End time
     * @param period Time type
     * @return
     */
    @ApiOperation(value = "Get coin historical K-line data")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "from", value = "Start time"),
            @ApiImplicitParam(name = "to", value = "End time"),
            @ApiImplicitParam(name = "period", value = "Time type"),
    })
    @RequestMapping("history4Feign")
    public List<KLine> findKHistory4Feign(
            @RequestParam("symbol") String symbol,
            @RequestParam("from") Long from,
            @RequestParam("to") Long to,
            @RequestParam("period") String period){
        List<KLine> list = marketService.findAllKLine(symbol,from,to,period);
        
        
        List<KLine> result = new ArrayList<>();
        boolean startFlag = false;
        KLine temKline = null;
        for(KLine item:list){
            // This section filters out K-lines at the beginning with open/close = 0
            if(!startFlag && item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }else {
                startFlag = true;
            }
            // If 0 appears in the middle section, adjust values
            if(item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                item.setOpenPrice(temKline.getClosePrice());
                item.setClosePrice(temKline.getClosePrice());
                item.setHighestPrice(temKline.getClosePrice());
                item.setLowestPrice(temKline.getClosePrice());
            }
            result.add(item);
            temKline = item;
        }
        
        return result;
    }
}
