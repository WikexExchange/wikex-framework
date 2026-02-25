package com.wikex.wikex.active.feign;

import com.wikex.wikex.active.entity.LockedOrderDetail;
import com.wikex.wikex.active.service.LockedOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("lockedOrderDetailFeign")
public class LockedOrderDetailFeignController {
    @Autowired
    private LockedOrderDetailService lockedOrderDetailService;
    @PostMapping("save")
    public LockedOrderDetail save(@RequestBody LockedOrderDetail detail) {
        lockedOrderDetailService.saveOrUpdate(detail);
        return detail;
    }
}
