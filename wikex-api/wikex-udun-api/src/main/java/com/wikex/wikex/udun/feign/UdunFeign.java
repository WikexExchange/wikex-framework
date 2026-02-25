package com.wikex.wikex.udun.feign;


import com.uduncloud.sdk.domain.Address;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient(value = "wikex-udun",contextId = "udunFeign")
public interface UdunFeign {

    @PostMapping(value = "/udun/create-address")
    Address createCoinAddress(@RequestParam("symbol") String symbol);

}
