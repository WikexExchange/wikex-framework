package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.LockedOrder;
import com.wikex.wikex.active.entity.MiningOrder;
import com.wikex.wikex.active.service.LockedOrderService;
import com.wikex.wikex.active.service.MiningOrderService;
import com.wikex.wikex.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("lockedOrderFeign")
public class LockedOrderFeignController extends BaseController {
	@Autowired
    private LockedOrderService lockedOrderService;
    @PostMapping("save")
    public LockedOrder save(@RequestBody LockedOrder order) {
        lockedOrderService.saveOrUpdate(order);
        return order;
    }

    @PostMapping("findAllByMemberIdAndActivityId")
    List<LockedOrder> findAllByMemberIdAndActivityId(@RequestParam("memberId") Long memberId, @RequestParam("activityId") Long activityId){
        return lockedOrderService.findAllByMemberIdAndActivityId(memberId,activityId);
    }

    @PostMapping("findAllByLockedStatus")
    List<LockedOrder> findAllByLockedStatus(@RequestParam("status") Integer status){
        return lockedOrderService.findAllByLockedStatus(status);
    }
}
