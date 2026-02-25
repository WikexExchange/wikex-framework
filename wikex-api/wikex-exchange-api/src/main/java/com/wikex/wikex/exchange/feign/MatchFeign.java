package com.wikex.wikex.exchange.feign;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("match-service")
public interface MatchFeign {
    @GetMapping("//{serviceName}/matchFeign/containsTrader")
    Boolean containsTrader(@PathVariable("serviceName")String serviceName,@RequestParam("symbol") String symbol);
    @GetMapping("//{serviceName}/matchFeign/start-trader")
    MessageResult startTrader(@PathVariable("serviceName")String serviceName, @RequestParam("symbol")String symbol);

    @GetMapping("//{serviceName}/matchFeign/stopTrader")
    void stopTrader(@PathVariable("serviceName")String serviceName, @RequestParam("symbol")String symbol);

    @GetMapping("//{serviceName}/matchFeign/resetTrader")
    void resetTrader(@PathVariable("serviceName")String serviceName, @RequestParam("symbol")String symbol);

    @GetMapping("//{serviceName}/matchFeign/engines")
    Map<String, Integer> engines(@PathVariable("serviceName") String serviceName);

    @GetMapping("//{serviceName}/matchFeign/plate")
    Map<String, List<TradePlateItem>> plate(@PathVariable("serviceName") String serviceName, @RequestParam("symbol")String symbol);

    @GetMapping("//{serviceName}/matchFeign/plate-mini")
    Map<String, Object> plateMini(@PathVariable("serviceName") String serviceName, @RequestParam("symbol")String symbol);

    @GetMapping("//{serviceName}/matchFeign/plate-full")
    Map<String, Object> plateFull(@PathVariable("serviceName") String serviceName, @RequestParam("symbol")String symbol);

    @GetMapping("//{serviceName}/matchFeign/getTradingStatus")
    Boolean getTradingStatus(@PathVariable("serviceName") String serviceName, @RequestParam("symbol")String symbol);

    @GetMapping("//{serviceName}/matchFeign/traderOverview")
    JSONObject traderOverview(@PathVariable("serviceName") String serviceName, @RequestParam("symbol") String symbol);

    @GetMapping("//{serviceName}/matchFeign/findOrder")
    ExchangeOrder findOrder(@PathVariable("serviceName") String serviceName, @RequestParam("symbol")String symbol,
                            @RequestParam("orderId")String orderId, @RequestParam("type")Integer type,
                            @RequestParam("direction")Integer direction);

    @GetMapping("//{serviceName}/matchFeign/trader-detail")
    JSONObject traderDetail(@PathVariable("serviceName") String serviceName,@RequestParam("symbol") String symbol);

    @GetMapping("//{serviceName}/matchFeign/symbols")
    List<String> symbols(@PathVariable("serviceName") String serviceName);
}
