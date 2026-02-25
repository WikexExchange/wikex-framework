package com.wikex.wikex.admin.controller.swap;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.TradingTimes;
import com.wikex.wikex.swap.feign.TradingTimesFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/tradingTimes")
@Slf4j
public class TradingTimesController extends BaseAdminController {

    @Autowired
    private TradingTimesFeign tradingTimesFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Get tradable time list
     * @return
     */
    @RequiresPermissions("trading-times:findByCoinId")
    @PostMapping("findByCoinId")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Get tradable time list")
    public MessageResult findByCoinId(@RequestParam("contractId") Long contractId){
        List<TradingTimes> times = tradingTimesFeign.findByCoinId(contractId);
        return success(times);
    }

    /**
     * Add or modify tradable time
     * @param tradingTimes
     * @return
     */
    @RequiresPermissions("trading-times:alter")
    @PostMapping("alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Add or modify tradable time")
    public MessageResult add(@Valid TradingTimes tradingTimes) {
        tradingTimesFeign.save(tradingTimes);
        return success();
    }

    /**
     * Get perpetual contract trading pair list
     * @param pageParam
     * @return
     */
    @RequiresPermissions("trading-time:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Options contract trading pair - List")
    public MessageResult list(PageParam pageParam) {
        Page<TradingTimes> coinList = tradingTimesFeign.findAll(pageParam);
        return success(IPage2Page(coinList));
    }

}
