package com.wikex.wikex.second.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "wikex-second",contextId = "contractSecondCoinFeign")
public interface ContractSecondCoinFeign {

    @PostMapping(value = "/coinFeign/findAll")
    Page<ContractSecondCoin> findAll(@RequestBody PageParam pageParam);

    @PostMapping(value = "/coinFeign/findOne")
    ContractSecondCoin findOne(@RequestParam("id") Long id);

    @PostMapping(value = "/coinFeign/findBySymbol")
    ContractSecondCoin findBySymbol(@RequestParam("symbol") String symbol);

    @PostMapping(value = "/coinFeign/save")
    ContractSecondCoin save(@RequestBody ContractSecondCoin contractCoin);
}
