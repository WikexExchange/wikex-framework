package com.wikex.wikex.second.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCycle;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "wikex-second",contextId = "contractSecondCycleFeign")
public interface ContractSecondCycleFeign {

    @PostMapping(value = "/contractSecondCycleFeign/findAll")
    Page<ContractSecondCycle> findAll(@RequestBody PageParam pageParam);

    @PostMapping(value = "/contractSecondCycleFeign/save")
    ContractSecondCycle save(@RequestBody ContractSecondCycle contractSecondCycle);

    @PostMapping(value = "/contractSecondCycleFeign/findOne")
    ContractSecondCycle findOne(@RequestParam("id") Long id);

    @PostMapping(value = "/contractSecondCycleFeign/deleteBatch")
    MessageResult deleteBatch(List<Long> delIds);
}
