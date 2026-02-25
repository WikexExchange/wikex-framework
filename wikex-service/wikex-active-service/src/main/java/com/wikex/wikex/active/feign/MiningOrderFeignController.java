package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.MiningOrder;
import com.wikex.wikex.active.service.MiningOrderService;
import com.wikex.wikex.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("miningOrderFeign")
public class MiningOrderFeignController extends BaseController {
	@Autowired
    private MiningOrderService miningOrderService;
    @PostMapping("save")
    public MiningOrder save(@RequestBody MiningOrder order) {
        miningOrderService.saveOrUpdate(order);
        return order;
    }

    @PostMapping("findAllByMemberIdAndActivityId")
    List<MiningOrder> findAllByMemberIdAndActivityId(@RequestParam("memberId") Long memberId, @RequestParam("activityId") Long activityId){
        return miningOrderService.findAllByMemberIdAndActivityId(memberId,activityId);
    }

    @PostMapping("findAllByMiningStatus")
    List<MiningOrder> findAllByMiningStatus(@RequestParam("status") Integer status){
        return miningOrderService.findAllByMiningStatus(status);
    }

}
