package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.AutoInvestActivity;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.screen.PageParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-earn",contextId = "autoInvestFeign")
public interface AutoInvestActivityFeign {

    @PostMapping(value = "/autoInvestActivityFeign/save")
    void save(@RequestBody AutoInvestActivity activity);

    @GetMapping(value = "/autoInvestActivityFeign/findById")
    AutoInvestActivity findById(@RequestParam("id") Long id);

    @PostMapping(value = "/autoInvestActivityFeign/findAll")
    Page<AutoInvestActivity> findAll(@RequestBody ActivityParam pageParam);
}
