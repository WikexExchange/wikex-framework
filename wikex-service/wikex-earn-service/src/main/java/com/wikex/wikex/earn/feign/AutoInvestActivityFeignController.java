package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.earn.entity.AutoInvestActivity;
import com.wikex.wikex.earn.service.AutoInvestActivityService;
import com.wikex.wikex.earn.vo.ActivityParam;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@Api(tags = "Entrusted Order Processing")

@Slf4j
@RestController
@RequestMapping("/autoInvestActivityFeign")
public class AutoInvestActivityFeignController extends BaseController {

    @Autowired
    private AutoInvestActivityService autoInvestActivityService;


    @PostMapping(value = "/save")
    void save(@RequestBody AutoInvestActivity activity){
        autoInvestActivityService.saveOrUpdate(activity);
    }

    @GetMapping(value = "/findById")
    AutoInvestActivity findById(@RequestParam("id") Long id){
        return autoInvestActivityService.getById(id);
    }

    @PostMapping(value = "/findAll")
    Page<AutoInvestActivity> findAll(@RequestBody ActivityParam pageParam){
        pageParam.setStatus(null);
        return autoInvestActivityService.findAll(pageParam);
    }

}
