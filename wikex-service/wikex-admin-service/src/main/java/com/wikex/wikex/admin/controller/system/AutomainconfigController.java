package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.vo.AutomainReadBlock;
import com.wikex.wikex.admin.vo.AutomainSetPassword;
import com.wikex.wikex.admin.vo.AutomainconfigVo;
import com.wikex.wikex.admin.vo.MessageEncrypt;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.rpc.feign.RpcFeign;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Automainconfig;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.feign.AutoMainConfigFeign;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.util.AESUtils;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.List;

/**
 * Coin Extension Management
 */
@Slf4j
@RestController
@RequestMapping("/system/automainconfig")
public class AutomainconfigController extends BaseAdminController {

    @Autowired
    private CoinFeign coinFeign;
    @Autowired
    private RpcFeign rpcFeign;
    @Autowired
    private CoinprotocolFeign coinprotocolFeign;
    @Autowired
    private AutoMainConfigFeign autoMainConfigFeign;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("system:automainconfig:coin-list")
    @GetMapping("/coin-list")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get coin list in collection configuration")
    public MessageResult coinList() {
        List<Coin> list = coinFeign.getAllCoinNameAndUnit();
        return success(list);
    }

    @RequiresPermissions("system:automainconfig:protocol-list")
    @GetMapping("/protocol-list")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get coin protocol list in collection configuration")
    public MessageResult protocolList() {
        List<CoinprotocolDTO> list = coinprotocolFeign.list();
        return success(list);
    }

    @RequiresPermissions("system:automainconfig:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get collection configuration list")
    public MessageResult pageQuery(PageParam pageParam) {
        Page<Automainconfig> pageResult = autoMainConfigFeign.findAll(pageParam.getPageNo(), pageParam.getPageSize());
        return success(IPage2Page(pageResult));
    }

    @RequiresPermissions("system:automainconfig:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create/Update collection configuration")
    public MessageResult merge(@Valid Automainconfig automainconfig, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        // Check if already exists
        Automainconfig one = autoMainConfigFeign.findAutoMainConfigByCoinNameAndProtocol(automainconfig.getCoinName(),
                automainconfig.getProtocol());
        if (automainconfig.getId() != null) {
            if (one != null && !one.getId().equals(automainconfig.getId())) {
                result = error(messageSource.getMessage("CURRENCY_ALREADY_EXISTS"));
                return result;
            }
        } else if (one != null) {
            result = error(messageSource.getMessage("The coin with the current protocol already exists"));
            return result;
        }

        // Delete redis cache
        redisTemplate.delete("automainconfig");

        automainconfig = autoMainConfigFeign.save(automainconfig);
        result = success(messageSource.getMessage("OPERATION_SUCCESS"));
        result.setData(automainconfig);
        return result;
    }

    @RequiresPermissions("system:automainconfig:collect-coin")
    @PostMapping("/collectCoin")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Manual collection")
    public MessageResult collectCoin(@Valid AutomainconfigVo automainconfig, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        Coinprotocol protocol = coinprotocolFeign.findByProtocol(automainconfig.getProtocol());
        // Remote RPC service URL, suffix is coin unit
        String chain = protocol.getSymbol().toLowerCase();
        MessageResult rt = rpcFeign.transferAll(chain, automainconfig.getAddress(), automainconfig.getCoinName(),
                automainconfig.getPassword().trim());
        if (rt == null) {
            return success(messageSource.getMessage("OPERATION_SUCCESS"));
        }
        if (rt.getCode() == 0) {
            return success(messageSource.getMessage("OPERATION_SUCCESS"));
        } else {
            return error(rt.getMessage());
        }
    }

    @RequiresPermissions("system:automainconfig:set-password")
    @PostMapping("/setPassword")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Set password")
    public MessageResult setPassword(AutomainSetPassword automainconfig) throws Exception {
        if (StringUtils.isEmpty(automainconfig.getPassword())) {
            return error(messageSource.getMessage("PASSWORD_CANNOT_BE_EMPTY"));
        }
        Coinprotocol protocol = coinprotocolFeign.findByProtocol(automainconfig.getProtocol());
        // Remote RPC service URL, suffix is coin unit
        String chain = protocol.getSymbol().toLowerCase();

        MessageResult mr = rpcFeign.setPassword(chain, automainconfig.getPassword().trim());
        if (mr == null) {
            return error(messageSource.getMessage("OPERATION_FAILED"));
        } else {
            if (mr.getCode() == 0) {
                return success(messageSource.getMessage("OPERATION_SUCCESS"));
            } else {
                return error(mr.getMessage());
            }
        }
    }

    @RequiresPermissions("system:automainconfig:update-contract")
    @PostMapping("/updateContract")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Sync coins")
    public MessageResult updateContract(AutomainSetPassword automainconfig) throws Exception {
        if (StringUtils.isEmpty(automainconfig.getPassword())) {
            return error(messageSource.getMessage("PASSWORD_CANNOT_BE_EMPTY"));
        }
        Coinprotocol protocol = coinprotocolFeign.findByProtocol(automainconfig.getProtocol());
        // Remote RPC service URL, suffix is coin unit
        String chain = protocol.getSymbol().toLowerCase();
        MessageResult mr = rpcFeign.updateContract(chain, automainconfig.getPassword().trim());
        if (mr == null) {
            return error(messageSource.getMessage("OPERATION_FAILED"));
        } else {
            if (mr.getCode() == 0) {
                return success(messageSource.getMessage("OPERATION_SUCCESS"));
            } else {
                return error(mr.getMessage());
            }
        }
    }

    @RequiresPermissions("system:automainconfig:encrypt")
    @PostMapping("/encrypt")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Encryption tool")
    public MessageResult encrypt(MessageEncrypt messageEncrypt) throws Exception {
        if (StringUtils.isEmpty(messageEncrypt.getPassword())) {
            return error(messageSource.getMessage("PASSWORD_CANNOT_BE_EMPTY"));
        }
        String encrypt = AESUtils.encrypt(messageEncrypt.getMessage(), messageEncrypt.getPassword().trim());
        return success(messageSource.getMessage("OPERATION_SUCCESS"), encrypt);
    }

    @RequiresPermissions("system:automainconfig:read-block")
    @PostMapping("/readBlock")
    @AccessLog(module = AdminModule.SYSTEM, operation = "重新读块")
    public MessageResult readBlock(AutomainReadBlock automainReadBlock) throws Exception {
        if (automainReadBlock.getBlockHeight() == null) {
            return error("区块高度不能为空");
        }
        Coinprotocol protocol = coinprotocolFeign.findByProtocol(automainReadBlock.getProtocol());
        // 远程RPC服务URL,后缀为币种单位
        String chain = protocol.getSymbol().toLowerCase();

        MessageResult mr = rpcFeign.readBlock(chain, automainReadBlock.getBlockHeight());
        if (mr == null) {
            return error(messageSource.getMessage("OPERATION_FAILED"));
        } else {
            if (mr.getCode() == 0) {
                return success(messageSource.getMessage("OPERATION_SUCCESS"));
            } else {
                return error(mr.getMessage());
            }
        }

    }

}
