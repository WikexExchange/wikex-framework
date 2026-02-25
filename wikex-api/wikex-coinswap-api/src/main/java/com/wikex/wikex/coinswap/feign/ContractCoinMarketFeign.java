package com.wikex.wikex.coinswap.feign;

import com.wikex.wikex.pojo.CoinThumb;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-coin-swap",contextId = "contractCoinMarket")
public interface ContractCoinMarketFeign {

    @RequestMapping("findSymbolThumb4Feign")
    List<CoinThumb> findSymbolThumb4Feign();
}
