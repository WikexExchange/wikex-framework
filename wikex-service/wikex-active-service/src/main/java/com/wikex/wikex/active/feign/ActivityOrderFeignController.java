package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.ActivityOrder;
import com.wikex.wikex.active.service.ActivityOrderService;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.WikexRuntimeException;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("activityOrderFeign")
public class ActivityOrderFeignController extends BaseController {
	@Autowired
    private ActivityOrderService activityOrderService;


    @PostMapping("findAllByActivityId")
    public List<ActivityOrder> findAllByActivityId(@RequestParam("aid")Long aid) {
        List<ActivityOrder> all = activityOrderService.findAllByActivityId(aid);
        return all;
    }

    @PostMapping("findById")
    public ActivityOrder findById(@RequestParam("id") Long id) {
        return activityOrderService.getById(id);
    }

    @PostMapping("save")
    public ActivityOrder save(@RequestBody ActivityOrder order) {
        activityOrderService.saveOrUpdate(order);
        return order;
    }

    @PostMapping("findAllByActivityIdAndMemberId")
    public List<ActivityOrder> findAllByActivityIdAndMemberId(@RequestParam("activityId")Long activityId,@RequestParam("memberId")Long memberId){
       return activityOrderService.findAllByActivityIdAndMemberId(memberId,activityId);
    }

    @PostMapping("saveActivityOrder")
    MessageResult saveActivityOrder(ActivityOrder activityOrder) throws WikexRuntimeException {
        return activityOrderService.saveActivityOrder(activityOrder);
    }
}
