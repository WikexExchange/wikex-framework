package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.AdvertiseControlStatus;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.p2p.entity.Advertise;
import com.wikex.wikex.screen.AdvertiseScreen;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-p2p",contextId = "advertiseFeign")
public interface AdvertiseFeign {
    @PostMapping(value = "/approveFeign/findOne")
    Advertise findOne(@RequestParam("id") Long id);

    @PostMapping(value = "/approveFeign/turnOffBatch")
    MessageResult turnOffBatch(@RequestParam(value = "status")AdvertiseControlStatus status,@RequestParam(value = "ids") Long[] ids);

    @PostMapping(value = "/approveFeign/findAll")
    Page<Advertise> findAll(AdvertiseScreen screen);

    @PostMapping(value = "/approveFeign/queryAdvertise")
    List<Advertise> queryAdvertise(@RequestParam("startTime")Date startTime, @RequestParam("endTime")Date endTime, @RequestParam("advertiseType")AdvertiseType advertiseType, @RequestParam("realName")String realName);

    @PostMapping(value = "/approveFeign/updateAdvertiseAmountForCancel")
    boolean updateAdvertiseAmountForCancel(@RequestParam("advertiseId")Long advertiseId,@RequestParam("amount") BigDecimal amount);

    @PostMapping(value = "/approveFeign/updateAdvertiseAmountForRelease")
    boolean updateAdvertiseAmountForRelease(@RequestParam("advertiseId")Long advertiseId,@RequestParam("amount") BigDecimal amount);
}
