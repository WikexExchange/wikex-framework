package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.LockedOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(value = "wikex-active",contextId = "lockedOrderFeign")
public interface LockedOrderFeign {

    @PostMapping("/lockedOrderFeign/save")
    LockedOrder save(@RequestBody LockedOrder order);

    @PostMapping("/lockedOrderFeign/findAllByMemberIdAndActivityId")
    List<LockedOrder> findAllByMemberIdAndActivityId(@RequestParam("memberId") Long memberId, @RequestParam("activityId") Long activityId);
    @PostMapping("/lockedOrderFeign/findAllByLockedStatus")
    List<LockedOrder> findAllByLockedStatus(@RequestParam("status") Integer status);
}
