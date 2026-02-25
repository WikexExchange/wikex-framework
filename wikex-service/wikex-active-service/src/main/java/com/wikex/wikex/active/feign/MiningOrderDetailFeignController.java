package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.MiningOrderDetail;
import com.wikex.wikex.active.service.MiningOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("miningOrderDetailFeign")
public class MiningOrderDetailFeignController {
    @Autowired
    private MiningOrderDetailService miningOrderDetailService;

    @PostMapping("save")
    public MiningOrderDetail save(@RequestBody MiningOrderDetail detail) {
        miningOrderDetailService.saveOrUpdate(detail);
        return detail;
    }
}
