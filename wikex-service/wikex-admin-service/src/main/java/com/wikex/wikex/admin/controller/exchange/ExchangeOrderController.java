package com.wikex.wikex.admin.controller.exchange;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.config.TradingConfig;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.ExchangeOrderStatus;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.screen.ExchangeOrderScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("exchange/exchange-order")
public class ExchangeOrderController extends BaseAdminController {

    @Autowired
    private ExchangeOrderFeign exchangeOrderService;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private TradingConfig tradingConfig;

    @RequiresPermissions("exchange:exchange-order:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "exchangeOrder details")
    public MessageResult detail(String id) {
        List<ExchangeOrderDetail> one = exchangeOrderService.findAllDetailByOrderId(id);
        if (one == null) {
            return error(messageSource.getMessage("NO_DATA"));
        }
        return success(one);
    }

    @RequiresPermissions("exchange:exchange-order:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Paginated query exchangeOrder")
    public MessageResult page(ExchangeOrderScreen screen) {
        Page<ExchangeOrder> all = exchangeOrderService.findAll(screen);
        return success(IPage2Page(all));
    }

    @RequiresPermissions("exchange:exchange-order:cancel")
    @PostMapping("cancel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Cancel order")
    public MessageResult cancelOrder(String orderId) {
        ExchangeOrder order = exchangeOrderService.findOne(orderId);
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, "order not in trading");
        }
        // Send message to Exchange system
        String serviceName = tradingConfig.getServiceName(order.getSymbol());
        rocketMQTemplate.convertAndSend("exchange-order-cancel-" + serviceName, JSON.toJSONString(order));
        return MessageResult.success(messageSource.getMessage("SUCCESS"));
    }
}
