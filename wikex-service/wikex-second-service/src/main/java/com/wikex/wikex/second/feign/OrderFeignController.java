package com.wikex.wikex.second.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractSecondOrderScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.wikex.wikex.second.entity.ContractSecondSet;
import com.wikex.wikex.second.service.ContractSecondOrderService;
import com.wikex.wikex.second.service.ContractSecondSetService;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;



@Slf4j
@RestController
@RequestMapping("/orderFeign")
public class OrderFeignController extends BaseController {

    @Autowired
    private ContractSecondOrderService contractSecondOrderService;
    @Autowired
    private ContractSecondSetService contractSecondSetService;

    @PostMapping(value = "findAll")
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ContractSecondOrder> findAll(@RequestBody ContractSecondOrderScreen screen){
        return contractSecondOrderService.findAll(screen);
    }

    @PostMapping(value = "updatePreClosePrice")
    public MessageResult updatePreClosePrice(@RequestParam("id") Long id, @RequestParam("presetPrice")BigDecimal presetPrice){
        contractSecondOrderService.updatePreClosePrice(id,presetPrice);
        return MessageResult.success();
    }

    @PostMapping(value = "findSecondSetAll")
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ContractSecondSet> findSecondSetAll(@RequestBody PageParam pageParam){
        return contractSecondSetService.findAll(pageParam);
    }

    @PostMapping(value = "addContractSecondSet")
    public ContractSecondSet addContractSecondSet(@RequestBody ContractSecondSet contractSecondSet){
        contractSecondSetService.saveOrUpdate(contractSecondSet);
        return contractSecondSet;
    }

    @PostMapping(value = "findContractSecondSetById")
    public ContractSecondSet findContractSecondSetById(@RequestParam("id") Long id){
        return contractSecondSetService.getById(id);
    }

    @PostMapping(value = "deleteSetBatch")
    public MessageResult deleteBatch(@RequestParam("ids")List<Long> ids){
        contractSecondSetService.removeByIds(ids);
        return MessageResult.success();
    }

}
