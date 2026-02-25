package com.wikex.wikex.market.feign;

import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.KLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-market",contextId = "marketFeign")
public interface MarketFeign {

    @RequestMapping("/marketFeign/engines")
    Map<String, Integer> engines();

    @RequestMapping("/marketFeign/symbolThumb4Feign")
    List<CoinThumb> findSymbolThumb4Feign();

    @RequestMapping("/marketFeign/history4Feign")
    List<KLine> findKHistory4Feign(
            @RequestParam("symbol") String symbol,
            @RequestParam("from") Long from,
            @RequestParam("to") Long to,
            @RequestParam("period") String period);

}
