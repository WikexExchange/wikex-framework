package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.RechargeScreen;
import com.wikex.wikex.user.entity.Recharge;
import com.wikex.wikex.user.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rechargeFeign")
public class RechargeFeignController extends BaseController {

    @Autowired
    private RechargeService rechargeService;

    @PostMapping("findAllOut")
    public List<Recharge> findAllOut(@RequestBody RechargeScreen screen) {
        return rechargeService.findAllOut(screen);
    }

    @PostMapping("findAll")
    public Page<Recharge> findAll(@RequestBody RechargeScreen rechargeScreen) {
        return rechargeService.findAll(rechargeScreen);
    }

}
