package com.wikex.wikex.swap.feign;

import com.wikex.wikex.pojo.CoinThumb;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-swap",contextId = "contractMarketFeign")
public interface ContractMarketFeign {

    @RequestMapping("findSymbolThumb4Feign")
    List<CoinThumb> findSymbolThumb4Feign();
}
