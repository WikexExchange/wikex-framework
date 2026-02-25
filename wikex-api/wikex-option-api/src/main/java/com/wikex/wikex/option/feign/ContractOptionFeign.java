package com.wikex.wikex.option.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.screen.ContractOptionScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-option",contextId = "contractOptionFeign")
public interface ContractOptionFeign {

    @PostMapping(value = "/optionFeign/findAll")
    Page<ContractOption> findAll(@RequestBody ContractOptionScreen pageParam);

    @PostMapping(value = "/optionFeign/findOne")
    ContractOption findOne(@RequestParam("id") Long id);

    @PostMapping(value = "/optionFeign/alert")
    MessageResult alert(@RequestBody ContractOption option);
}
