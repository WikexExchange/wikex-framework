package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Coin Protocol Management
 */
@Slf4j
@RestController
@RequestMapping("/system/coinprotocol")
public class CoinProtocolController extends BaseAdminController {

    @Autowired
    private CoinprotocolFeign coinprotocolService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("system:coinprotocol:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get all protocol list")
    public MessageResult pageQuery(PageParam pageParam) {
        Page<Coinprotocol> pageResult = coinprotocolService.findAll(pageParam.getPageNo(), pageParam.getPageSize());
        return success(IPage2Page(pageResult));
    }

    @RequiresPermissions("system:coinprotocol:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create/Update protocol")
    public MessageResult merge(@Valid Coinprotocol coinprotocol, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        // Check if protocol already exists
        Coinprotocol one = coinprotocolService.findByProtocol(coinprotocol.getProtocol());
        if (coinprotocol.getId() != null) {
            if (one != null && !one.getId().equals(coinprotocol.getId())) {
                result = error(messageSource.getMessage("CURRENT_PROTOCOL_ALREADY_EXISTS"));
                return result;
            }
        } else if (one != null) {
            result = error(messageSource.getMessage("CURRENT_PROTOCOL_ALREADY_EXISTS"));
            return result;
        }

        // Delete redis cache
        redisTemplate.delete("coinprotocol");

        coinprotocol = coinprotocolService.save(coinprotocol);

        result = success(messageSource.getMessage("OPERATION_SUCCESS"));
        result.setData(coinprotocol);
        return result;
    }
}
