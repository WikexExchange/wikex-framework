package com.wikex.wikex.admin.controller.swap;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.swap.feign.ContractOrderEntrustFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contract Order Entrust Management Controller
 * Handles admin operations for perpetual contract entrust orders
 */
@RestController
@RequestMapping("/swap/order")
@Slf4j
public class ContractOrderEntrustController extends BaseAdminController {

    @Autowired
    private ContractOrderEntrustFeign contractOrderEntrustService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Paginated query for entrust orders
     * @param screen filter conditions
     * @return paginated result of contract order entrusts
     */
    @RequiresPermissions("swap:order:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "List perpetual contract entrust orders")
    public MessageResult pageQuery(ContractOrderEntrustScreen screen) {
        Page<ContractOrderEntrust> all = contractOrderEntrustService.pageQuery(screen);
        return success(IPage2Page(all));
    }

    /**
     * Cancel an entrust order
     * @param orderId ID of the order to cancel
     * @return success or failure message
     */
    @RequiresPermissions("swap:order:cancel")
    @PostMapping("cancel")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Cancel perpetual contract order")
    public MessageResult cancelOrder(Long orderId) {
        ContractOrderEntrust order = contractOrderEntrustService.findOne(orderId);
        if (order == null) {
            return MessageResult.error(messageSource.getMessage("CANCEL_ORDER_FAILED"));
        }
        if (order.getStatus() != ContractOrderEntrustStatus.ENTRUST_ING) {
            return MessageResult.error(messageSource.getMessage("DELEGATE_STATUS_ERROR"));
        }
        // Send cancel message to Exchange system
        rocketMQTemplate.convertAndSend("swap-order-cancel", JSON.toJSONString(order));

        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }
}
