package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.wikex.wikex.earn.vo.ActivityParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-earn",contextId = "autoInvestPlanFeign")
public interface AutoInvestPlanFeign {

    @PostMapping(value = "/autoInvestPlanFeign/findAll")
    Page<AutoInvestPlan> findAll(ActivityParam pageParam);
}
