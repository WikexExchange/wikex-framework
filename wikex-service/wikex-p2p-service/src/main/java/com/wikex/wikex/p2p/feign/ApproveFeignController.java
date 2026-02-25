package com.wikex.wikex.p2p.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.AdvertiseControlStatus;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.p2p.entity.Advertise;
import com.wikex.wikex.p2p.service.AdvertiseService;
import com.wikex.wikex.screen.AdvertiseScreen;
import com.wikex.wikex.util.MessageResult;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;



@RestController
@RequestMapping("/approveFeign")
@Slf4j
public class ApproveFeignController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(ApproveFeignController.class);


    @Autowired
    private AdvertiseService advertiseService;

    @PostMapping(value = "/findOne")
    public Advertise findOne(@RequestParam("id") Long id){
        return advertiseService.getById(id);
    }

    @PostMapping(value = "/turnOffBatch")
    @GlobalTransactional
    public MessageResult turnOffBatch(@RequestParam(value = "status") AdvertiseControlStatus status,
                                      @RequestParam(value = "ids") Long[] ids){
        advertiseService.turnOffBatch(status,ids);
        return MessageResult.success();
    }

    @PostMapping(value = "/findAll")
    public Page<Advertise> findAll(@RequestBody AdvertiseScreen screen){
        return advertiseService.findAll(screen);
    }

    @PostMapping(value = "/queryAdvertise")
    public List<Advertise> queryAdvertise(@RequestParam("startTime")Date startTime,
                                          @RequestParam("endTime")Date endTime,
                                          @RequestParam("advertiseType")AdvertiseType advertiseType,
                                          @RequestParam("realName")String realName){
        return advertiseService.queryAdvertise(startTime,endTime,advertiseType,realName);
    }

    @PostMapping(value = "/updateAdvertiseAmountForCancel")
    public boolean updateAdvertiseAmountForCancel(@RequestParam("advertiseId")Long advertiseId,
                                                  @RequestParam("amount") BigDecimal amount){
        return advertiseService.updateAdvertiseAmountForCancel(advertiseId,amount);
    }

    @PostMapping(value = "/approve/updateAdvertiseAmountForRelease")
    public boolean updateAdvertiseAmountForRelease(@RequestParam("advertiseId")Long advertiseId,
                                                   @RequestParam("amount") BigDecimal amount){
        return advertiseService.updateAdvertiseAmountForRelease(advertiseId,amount);
    }

}
