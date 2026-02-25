package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractCoin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-swap",contextId = "swapCoinFeign")
public interface ContractCoinFeign {

    @PostMapping(value = "/coinFeign/findAll")
    Page<ContractCoin> findAll(@RequestBody PageParam pageParam);

    @PostMapping(value = "/coinFeign/findOne")
    ContractCoin findOne(@RequestParam("contractId") Long contractId);

    @PostMapping(value = "/coinFeign/findBySymbol")
    ContractCoin findBySymbol(@RequestParam("symbol")String symbol);

    @PostMapping(value = "/coinFeign/save")
    ContractCoin save(@RequestBody ContractCoin contractCoin);
}
