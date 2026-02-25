package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.MiningOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(value = "wikex-active",contextId = "miningOrderFeign")
public interface MiningOrderFeign {

    @PostMapping("/miningOrderFeign/save")
    MiningOrder save(@RequestBody MiningOrder order);

    @PostMapping("/miningOrderFeign/findAllByMemberIdAndActivityId")
    List<MiningOrder> findAllByMemberIdAndActivityId(@RequestParam("memberId") Long memberId, @RequestParam("activityId") Long activityId);

    @PostMapping("/miningOrderFeign/findAllByMiningStatus")
    List<MiningOrder> findAllByMiningStatus(@RequestParam("status") Integer status);
}
