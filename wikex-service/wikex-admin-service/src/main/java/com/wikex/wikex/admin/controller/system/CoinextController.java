package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.dto.CoinDTO;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.screen.CoinextScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Coinext;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.CoinextFeign;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Coin Extension Management
 */
@Slf4j
@RestController
@RequestMapping("/system/coinext")
public class CoinextController extends BaseAdminController {

    @Autowired
    private CoinFeign coinFeign;

    @Autowired
    private CoinprotocolFeign coinprotocolFeign;

    @Autowired
    private CoinextFeign coinextFeign;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("system:coinext:coin-list")
    @GetMapping("/coin-list")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get coin list in Coin Extension")
    public MessageResult coinList() {
        List<Coin> list = coinFeign.getAllCoinNameAndUnit();
        return success(list);
    }

    @RequiresPermissions("system:coinext:protocol-list")
    @GetMapping("/protocol-list")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get coin protocol list in Coin Extension")
    public MessageResult protocolList() {
        List<CoinprotocolDTO> list = coinprotocolFeign.list();
        return success(list);
    }

    @RequiresPermissions("system:coinext:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get Coin Extension list")
    public MessageResult pageQuery(CoinextScreen coinextScreen) {
        Page<Coinext> pageResult = coinextFeign.findAll(coinextScreen);
        return success(IPage2Page(pageResult));
    }

    @RequiresPermissions("system:coinext:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create/Update Coin Extension")
    public MessageResult merge(@Valid Coinext coinext, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        // Check if record already exists
        Coinext one = coinextFeign.findFirstByCoinNameAndProtocol(coinext.getCoinName(), coinext.getProtocol());
        if (coinext.getId() != null) {
            if (one != null && !one.getId().equals(coinext.getId())) {
                result = error(messageSource.getMessage("CURRENCY_ALREADY_EXISTS"));
                return result;
            }
        } else if (one != null) {
            result = error(messageSource.getMessage("CURRENCY_ALREADY_EXISTS"));
            return result;
        }

        Coinprotocol byProtocol = coinprotocolFeign.findByProtocol(coinext.getProtocol());
        if (byProtocol == null) {
            result = error(messageSource.getMessage("CURRENT_PROTOCOL_NOT_FOUND"));
            return result;
        }

        coinext.setProtocolName(byProtocol.getProtocolName());

        // Delete redis cache
        redisTemplate.delete("coinext");

        coinext = coinextFeign.save(coinext);
        result = success(messageSource.getMessage("OPERATION_SUCCESS"));
        result.setData(coinext);
        return result;
    }
}
