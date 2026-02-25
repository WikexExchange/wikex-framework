package com.wikex.wikex.option.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.service.ContractOptionService;
import com.wikex.wikex.screen.ContractOptionScreen;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("optionFeign")
public class ContractOptionFeignController extends BaseController {

    @Autowired
    private ContractOptionService optionService;

    @PostMapping(value = "findAll")
    public Page<ContractOption> findAll(@RequestBody ContractOptionScreen screen){
        return optionService.findAll(screen);
    }

    @PostMapping(value = "findOne")
    public ContractOption findOne(@RequestParam("id") Long id){
        return optionService.findOne(id);
    }

    @PostMapping(value = "alert")
    public MessageResult alert(@RequestBody ContractOption option){
        boolean b = optionService.updateById(option);
        return success(b);
    }
}
