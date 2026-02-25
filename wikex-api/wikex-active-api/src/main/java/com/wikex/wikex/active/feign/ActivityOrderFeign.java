package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.ActivityOrder;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(value = "wikex-active",contextId = "activityOrderFeign")
public interface ActivityOrderFeign {


    @PostMapping("/activityOrderFeign/findAllByActivityId")
    List<ActivityOrder> findAllByActivityId(@RequestParam("aid")Long aid);

    @PostMapping("/activityOrderFeign/findById")
    ActivityOrder findById(@RequestParam("id")Long id);

    @PostMapping("/activityOrderFeign/save")
    ActivityOrder save(ActivityOrder order);

    @PostMapping("/activityOrderFeign/findAllByActivityIdAndMemberId")
    List<ActivityOrder> findAllByActivityIdAndMemberId(@RequestParam("activityId")Long activityId,@RequestParam("memberId")Long memberId);

    @PostMapping("/activityOrderFeign/saveActivityOrder")
    MessageResult saveActivityOrder(ActivityOrder activityOrder);
}
