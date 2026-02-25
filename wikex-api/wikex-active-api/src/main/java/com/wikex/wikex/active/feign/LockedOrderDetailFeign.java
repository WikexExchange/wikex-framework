package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.LockedOrderDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "wikex-active",contextId = "lockedOrderDetailFeign")
public interface LockedOrderDetailFeign {
    @PostMapping("/lockedOrderDetailFeign/save")
    LockedOrderDetail save(@RequestBody LockedOrderDetail detail);
}
