package com.wikex.wikex.active.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.active.service.ActivityService;
import com.wikex.wikex.active.entity.Activity;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("activityFeign")
public class ActivityFeignController extends BaseController {
    @Autowired
    private ActivityService activityService;


    // @ApiOperation(value = "Query locked activities that are currently in progress")
    @PostMapping("lockedActivityList")
    public List<Activity> lockedActivityList() {
        List<Activity> all = activityService.findByTypeAndStep(6, 1); // Query locked activities currently in progress
        return all;
    }

    @PostMapping("findAll")
    public Page<Activity> findAll(@RequestBody PageParam pageParam) {
        Page<Activity> all = activityService.findAll(pageParam);
        return all;
    }

    @PostMapping("save")
    public Activity save(@RequestBody Activity activity) {
        activityService.saveOrUpdate(activity);
        return activity;
    }

    @PostMapping("findById")
    public Activity findById(@RequestParam("id") Long id) {

        return activityService.getById(id);
    }
}
