package com.wikex.wikex.active.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.active.entity.Activity;
import com.wikex.wikex.screen.PageParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(value = "wikex-active",contextId = "activityFeign")
public interface ActivityFeign {

    @PostMapping("/activityFeign/lockedActivityList")
    List<Activity> lockedActivityList();

    @PostMapping("/activityFeign/findAll")
    Page<Activity> findAll(@RequestBody PageParam pageParam);

    @PostMapping("/activityFeign/save")
    Activity save(@RequestBody Activity activity);

    @PostMapping("/activityFeign/findById")
    Activity findById(@RequestParam("id") Long id);
}
