package com.wikex.wikex.second.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondSet;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "wikex-second",contextId = "contractSecondSetFeign")
public interface ContractSecondSetFeign {

    @PostMapping(value = "/orderFeign/findSecondSetAll")
    Page<ContractSecondSet> findSecondSetAll(@RequestBody PageParam pageParam);

    @PostMapping(value = "/orderFeign/addContractSecondSet")
    ContractSecondSet save(@RequestBody ContractSecondSet contractSecondSet);

    @PostMapping(value = "/orderFeign/findContractSecondSetById")
    ContractSecondSet findOne(@RequestParam("id")Long id);

    @PostMapping(value = "/orderFeign/deleteSetBatch")
    MessageResult deleteBatch(@RequestParam("ids")List<Long> ids);
}
