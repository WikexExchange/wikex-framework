package com.wikex.wikex.admin.controller.coinswap;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.wikex.wikex.coinswap.feign.ContractCoinOrderEntrustFeign;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.screen.ContractOrderEntrustCoinScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coinswap/order")
@Slf4j
public class ContractOrderEntrustCoinController extends BaseAdminController {
    @Autowired
    private ContractCoinOrderEntrustFeign contractOrderEntrustService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Paginated query
     * @param screen query filter
     * @return result
     */
    @RequiresPermissions("coinswap:order:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract orders - list")
    public MessageResult pageQuery(ContractOrderEntrustCoinScreen screen) {
        Page<ContractOrderEntrustCoin> all = contractOrderEntrustService.pageQuery(screen);
        return success(IPage2Page(all));
    }

    /**
     * Cancel entrust order
     * @param orderId order ID
     * @return result
     */
    @RequiresPermissions("coinswap:order:cancel")
    @PostMapping("cancel")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract - cancel order")
    public MessageResult cancelOrder(Long orderId) {
        ContractOrderEntrustCoin order = contractOrderEntrustService.findOne(orderId);
        if (order == null) {
            return MessageResult.error(messageSource.getMessage("CANCEL_ORDER_FAILED"));
        }
        if (order.getStatus() != ContractOrderEntrustStatus.ENTRUST_ING) {
            return MessageResult.error(messageSource.getMessage("CONTRACT_INVALID_ORDER_STATUS"));
        }
        // Send message to Exchange system
        rocketMQTemplate.convertAndSend("swap-coin-order-cancel", JSON.toJSONString(order));

        
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }
}
