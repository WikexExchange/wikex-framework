package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.wikex.wikex.earn.service.AutoInvestPlanService;
import com.wikex.wikex.earn.vo.ActivityParam;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@Api(tags = "Entrusted Order Processing")

@Slf4j
@RestController
@RequestMapping("/autoInvestPlanFeign")
public class AutoInvestPlanFeignController extends BaseController {

    @Autowired
    private AutoInvestPlanService autoInvestPlanService;


    @PostMapping(value = "/findAll")
    Page<AutoInvestPlan> findAll(@RequestBody ActivityParam pageParam){
        return autoInvestPlanService.findAll4Admin(pageParam);
    }

}
