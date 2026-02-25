package com.wikex.wikex.second.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCycle;
import com.wikex.wikex.second.service.ContractSecondCycleService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@Slf4j
@RestController
@RequestMapping("/contractSecondCycleFeign")
public class ContractSecondCycleFeignController extends BaseController {
    @Autowired
    private ContractSecondCycleService contractSecondCycleService;
    @Autowired
    private LocaleMessageSourceService msService;

    @PostMapping(value = "findAll")
    public Page<ContractSecondCycle> findAll(@RequestBody PageParam pageParam){
        return contractSecondCycleService.findAll(pageParam);
    }

    @PostMapping(value = "save")
    public ContractSecondCycle save(@RequestBody ContractSecondCycle contractSecondCycle){
        contractSecondCycleService.saveOrUpdate(contractSecondCycle);
        return contractSecondCycle;
    }

    @PostMapping(value = "findOne")
    public ContractSecondCycle findOne(@RequestParam("id") Long id){
        return contractSecondCycleService.findOne(id);
    }

    @PostMapping(value = "deleteBatch")
    public MessageResult deleteBatch(List<Long> delIds){
        contractSecondCycleService.removeByIds(delIds);
        return MessageResult.success();
    }
}
