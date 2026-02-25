package com.wikex.wikex.coinswap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractOrderEntrustCoinScreen;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "wikex-coin-swap", contextId = "contractCoinOrderFeign")
public interface ContractCoinOrderEntrustFeign {

    /**
     * Paginated query for perpetual contract entrust orders.
     *
     * @param screen filter and pagination conditions
     * @return a page of contract entrust orders
     */
    @PostMapping("/orderFeign/page-query")
    Page<ContractOrderEntrustCoin> pageQuery(@RequestBody ContractOrderEntrustCoinScreen screen);

    @GetMapping("/orderFeign/findOne")
    ContractOrderEntrustCoin findOne(@RequestParam("id") Long id);
}
