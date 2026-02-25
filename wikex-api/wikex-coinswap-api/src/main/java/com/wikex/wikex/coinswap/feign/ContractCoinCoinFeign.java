package com.wikex.wikex.coinswap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "wikex-coin-swap",contextId = "contractCoinCoinFeign")
public interface ContractCoinCoinFeign {

    @PostMapping(value = "/coinFeign/findAll")
    Page<ContractCoinCoin> findAll(@RequestBody PageParam pageParam);
    @PostMapping(value = "/coinFeign/findOne")
    ContractCoinCoin findOne(@RequestParam("contractId") Long contractId);

    @PostMapping(value = "/coinFeign/findBySymbol")
    ContractCoinCoin findBySymbol(@RequestParam("symbol")String symbol);

    @PostMapping(value = "/coinFeign/save")
    ContractCoinCoin save(@RequestBody ContractCoinCoin contractCoin);
}
