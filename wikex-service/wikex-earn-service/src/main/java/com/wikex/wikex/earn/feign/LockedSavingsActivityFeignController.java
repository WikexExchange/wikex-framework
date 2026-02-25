package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.earn.entity.LockedSavingsActivity;
import com.wikex.wikex.earn.service.LockedSavingsActivityService;
import com.wikex.wikex.earn.vo.ActivityParam;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@Api(tags = "Entrusted Order Processing")
@Slf4j
@RestController
@RequestMapping("/lockedSavingsActivityFeign")
public class LockedSavingsActivityFeignController extends BaseController {

    @Autowired
    private LockedSavingsActivityService lockedSavingsActivityService;

    @PostMapping(value = "/save")
    void save(@RequestBody LockedSavingsActivity activity){
        lockedSavingsActivityService.saveOrUpdate(activity);
    }

    @GetMapping(value = "/findById")
    LockedSavingsActivity findById(@RequestParam("id") Long id){
        return lockedSavingsActivityService.getById(id);
    }

    @PostMapping(value = "/findAll")
    Page<LockedSavingsActivity> findAll(@RequestBody ActivityParam pageParam){
        pageParam.setStatus(null);
        return lockedSavingsActivityService.findAll(pageParam);
    }
}
