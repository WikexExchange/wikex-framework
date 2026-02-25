package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.earn.entity.LockedSavingsActivity;
import com.wikex.wikex.earn.entity.LockedSavingsOrder;
import com.wikex.wikex.earn.service.LockedSavingsActivityService;
import com.wikex.wikex.earn.service.LockedSavingsOrderService;
import com.wikex.wikex.earn.vo.ActivityParam;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@Api(tags = "Entrusted Order Processing")

@Slf4j
@RestController
@RequestMapping("/lockedSavingsOrderFeign")
public class LockedSavingsOrderFeignController extends BaseController {

    @Autowired
    private LockedSavingsOrderService lockedSavingsOrderService;


    @PostMapping(value = "/findAll")
    Page<LockedSavingsOrder> findAll(@RequestBody ActivityParam pageParam){
        return lockedSavingsOrderService.findAll(pageParam);
    }
}
