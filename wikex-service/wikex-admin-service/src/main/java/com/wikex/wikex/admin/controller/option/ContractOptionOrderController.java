package com.wikex.wikex.admin.controller.option;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.wikex.wikex.option.feign.ContractOptionOrderFeign;
import com.wikex.wikex.screen.ContractOptionOrderScreen;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/option/order")
@Slf4j
public class ContractOptionOrderController extends BaseAdminController {

    @Autowired
    private ContractOptionOrderFeign contractOptionOrderService;

    /**
     * Paginated query of all orders
     * @param screen
     * @return
     */
    @RequiresPermissions("option:order:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Option contract order list")
    public MessageResult detail(
            ContractOptionOrderScreen screen) {
        Page<ContractOptionOrder> all = contractOptionOrderService.findAll(screen);
        return success(IPage2Page(all));
    }

    /**
     * All orders of a specific option contract
     * @param optionId
     * @return
     */
    @RequiresPermissions("option:order:option-list")
    @PostMapping("option-list")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Option contract order list")
    public MessageResult queryByOptionId(Long optionId){
        List<ContractOptionOrder> list = contractOptionOrderService.findByOptionId(optionId);
        return success(list);
    }

    /**
     * All orders of a specific member
     * @param memberId
     * @return
     */
    @RequiresPermissions("option:order:member-list")
    @PostMapping("member-list")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Option contract order list")
    public MessageResult queryByMember(Long memberId){
        List<ContractOptionOrder> list = contractOptionOrderService.findByMemberId(memberId);
        return success(list);
    }

    /**
     * Set option contract order
     * @param memberId
     * @param optionNo
     * @param optionNoChange
     * @param directionChange
     * @return
     */
    @RequiresPermissions("option:order:setOptionOrder")
    @PostMapping("setOptionOrder")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Set option contract order")
    public MessageResult setOptionOrder(Long memberId,Integer optionNo,Short optionNoChange,Short directionChange){
        return contractOptionOrderService.setOptionOrder(memberId,optionNo,optionNoChange,directionChange);
    }

}
