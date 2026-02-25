package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.CoinInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "wikex-user", contextId = "coinInfoFeign")
public interface CoinInfoFeign {

    @GetMapping("/coinInfoFeign/findByUnit")
    CoinInfo findByUnit(@RequestParam("unit") String unit);

    @GetMapping("/coinInfoFeign/findByCoinId")
    CoinInfo findByCoinId(@RequestParam("coinId") Long coinId);

    @PostMapping("/coinInfoFeign/save")
    CoinInfo save(@RequestBody CoinInfo coinInfo);

    @PostMapping("/coinInfoFeign/saveOrUpdate")
    CoinInfo saveOrUpdate(@RequestBody CoinInfo coinInfo);
}