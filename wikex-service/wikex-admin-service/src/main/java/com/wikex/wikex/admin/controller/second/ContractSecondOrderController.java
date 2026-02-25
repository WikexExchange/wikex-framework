package com.wikex.wikex.admin.controller.second;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.ContractSecondOrderScreen;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.wikex.wikex.second.feign.ContractSecondOrderFeign;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/second-order")
@Slf4j
public class ContractSecondOrderController extends BaseAdminController {

    @Autowired
    private ContractSecondOrderFeign contractSecondOrderService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Paginated query of all orders
     * @param screen
     * @return
     */
    @RequiresPermissions("second:order:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Second option contract order list")
    public MessageResult detail(
            ContractSecondOrderScreen screen) {
        Page<ContractSecondOrder> all = contractSecondOrderService.findAll(screen);
        return success(IPage2Page(all));
    }

    /**
     * Set preset closing price
     * @return
     */
    @RequiresPermissions("second:order:alter")
    @PostMapping("alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Set preset closing price")
    public MessageResult alter(
            @RequestParam(value = "id",required = true) Long id,
            @RequestParam(value = "preClosePrice", required = true) BigDecimal presetPrice){
        contractSecondOrderService.updatePreClosePrice(id, presetPrice);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }

}
