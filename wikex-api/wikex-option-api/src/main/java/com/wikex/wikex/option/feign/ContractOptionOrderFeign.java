package com.wikex.wikex.option.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.wikex.wikex.screen.ContractOptionOrderScreen;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-option",contextId = "contractOptionOrderFeign")
public interface ContractOptionOrderFeign {

    @PostMapping(value = "/orderFeign/findAll")
    Page<ContractOptionOrder> findAll(@RequestBody ContractOptionOrderScreen screen);

    @PostMapping(value = "/orderFeign/findByOptionId")
    List<ContractOptionOrder> findByOptionId(@RequestParam("optionId") Long optionId);

    @PostMapping(value = "/orderFeign/findByMemberId")
    List<ContractOptionOrder> findByMemberId(@RequestParam("memberId")Long memberId);

    @PostMapping(value = "/orderFeign/setOptionOrder")
    MessageResult setOptionOrder(@RequestParam("memberId")Long memberId,@RequestParam("optionNo") Integer optionNo,@RequestParam("optionNoChange") Short optionNoChange,@RequestParam("directionChange")Short directionChange);
}
