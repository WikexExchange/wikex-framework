package com.wikex.wikex.second.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractSecondOrderScreen;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(value = "wikex-second",contextId = "contractSecondOrderFeign")
public interface ContractSecondOrderFeign {

    @PostMapping(value = "/orderFeign/findAll")
    Page<ContractSecondOrder> findAll(@RequestBody ContractSecondOrderScreen screen);

    @PostMapping(value = "/orderFeign/updatePreClosePrice")
    MessageResult updatePreClosePrice(@RequestParam("id") Long id, @RequestParam("presetPrice")BigDecimal presetPrice);
}
