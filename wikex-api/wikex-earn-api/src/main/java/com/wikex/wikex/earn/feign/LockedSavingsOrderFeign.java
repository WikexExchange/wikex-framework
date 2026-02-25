package com.wikex.wikex.earn.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.wikex.wikex.earn.entity.LockedSavingsOrder;
import com.wikex.wikex.earn.vo.ActivityParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-earn",contextId = "lockedSavingsOrderFeign")
public interface LockedSavingsOrderFeign {

    @PostMapping(value = "/lockedSavingsOrderFeign/findAll")
    Page<LockedSavingsOrder> findAll(@RequestBody ActivityParam pageParam);
}
