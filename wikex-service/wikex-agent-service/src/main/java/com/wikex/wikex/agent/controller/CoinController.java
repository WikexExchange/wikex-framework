package com.wikex.wikex.agent.controller;

import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description Backend Coin Web
 * @date 2019/12/29 15:01
 */
@RestController
@RequestMapping("coin")
@Slf4j
public class CoinController extends BaseController {

    @Autowired
    private CoinFeign coinFeign;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Get all coin names
     */
    @PostMapping("all-name")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Find all coin names")
    public MessageResult getAllCoinName() {
        List<String> list = coinFeign.getAllCoinName();
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * Get all coin names and units
     */
    @PostMapping("all-name-and-unit")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Find all coin names and units")
    public MessageResult getAllCoinNameAndUnit() {
        List<Coin> list = coinFeign.getAllCoinNameAndUnit();
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), list);
    }
}
