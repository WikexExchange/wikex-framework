package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.LockedSavingsActivity;
import com.wikex.wikex.earn.vo.ActivityParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-earn",contextId = "lockedSavingsActivityFeign")
public interface LockedSavingsActivityFeign {

    @PostMapping(value = "/lockedSavingsActivityFeign/save")
    void save(@RequestBody LockedSavingsActivity activity);

    @GetMapping(value = "/lockedSavingsActivityFeign/findById")
    LockedSavingsActivity findById(@RequestParam("id") Long id);

    @PostMapping(value = "/lockedSavingsActivityFeign/findAll")
    Page<LockedSavingsActivity> findAll(@RequestBody ActivityParam pageParam);
}
