package com.wikex.wikex.option.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.wikex.wikex.option.service.ContractOptionOrderService;
import com.wikex.wikex.screen.ContractOptionOrderScreen;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/orderFeign")
public class OrderFeignController extends BaseController {

    @Autowired
    private ContractOptionOrderService contractOptionOrderService;


    @PostMapping(value = "findAll")
    public Page<ContractOptionOrder> findAll(@RequestBody ContractOptionOrderScreen screen){
        return contractOptionOrderService.findAll(screen);
    }

    @PostMapping(value = "findByOptionId")
    public List<ContractOptionOrder> findByOptionId(@RequestParam("optionId") Long optionId){
        return contractOptionOrderService.findByOptionId(optionId);
    }

    @PostMapping(value = "findByMemberId")
    public List<ContractOptionOrder> findByMemberId(@RequestParam("memberId")Long memberId){
        return contractOptionOrderService.findByMemberId(memberId);
    }

    @PostMapping(value = "/order/setOptionOrder")
    MessageResult setOptionOrder(@RequestParam("memberId")Long memberId,
                                 @RequestParam("optionNo") Integer optionNo,
                                 @RequestParam("optionNoChange") Short optionNoChange,
                                 @RequestParam("directionChange")Short directionChange){
        return contractOptionOrderService.setOptionOrder(memberId,optionNo,optionNoChange,directionChange);
    }
}
