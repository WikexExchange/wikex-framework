package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.RechargeScreen;
import com.wikex.wikex.user.entity.Recharge;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "rechargeFeign")
public interface RechargeFeign {

    @PostMapping("/rechargeFeign/findAllOut")
    List<Recharge> findAllOut(@RequestBody RechargeScreen screen);

    @PostMapping("/rechargeFeign/findAll")
    Page<Recharge> findAll(@RequestBody RechargeScreen rechargeScreen);
}
