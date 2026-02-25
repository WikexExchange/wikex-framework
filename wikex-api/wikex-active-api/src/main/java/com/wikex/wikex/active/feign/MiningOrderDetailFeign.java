package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.MiningOrderDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "wikex-active",contextId = "miningOrderDetailFeign")
public interface MiningOrderDetailFeign {
    @PostMapping("/miningOrderDetailFeign/save")
    MiningOrderDetail save(@RequestBody MiningOrderDetail detail);
}
