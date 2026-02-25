package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author Hevin
 * @description Backend Currency Web Controller
 * @date 2019/12/29 15:01
 */
@RestController
@RequestMapping("/system/coin")
@Slf4j
public class CoinController extends BaseAdminController {

    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class);

    @Autowired
    private CoinFeign coinService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequiresPermissions("system:coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paginated query for backend Coin")
    public MessageResult pageQuery(PageParam pageParam) {
        Page<Coin> pageResult = coinService.findAll(pageParam.getPageNo(), pageParam.getPageSize());
        return success(IPage2Page(pageResult));
    }

    @RequiresPermissions("system:coin:create")
    @PostMapping("create")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create backend Coin")
    public MessageResult create(@Valid Coin coin, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        if ("decp".equalsIgnoreCase(coin.getName().trim()) || "dcep".equalsIgnoreCase(coin.getName().trim())) {
            return error(msService.getMessage("COIN_NAME_EXIST"));
        }

        Coin one = coinService.findByCoinId(coin.getName());
        if (one != null) {
            return error(msService.getMessage("COIN_NAME_EXIST"));
        }
        coinService.save(coin);
        return success();
    }

    @RequiresPermissions("system:coin:page-query")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get backend Coin details")
    public MessageResult detail(@Valid Coin coin, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Coin one = coinService.findByCoinId(coin.getName());
        return success(one);
    }

    @RequiresPermissions("system:coin:update")
    @PostMapping("update")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Update backend Coin")
    public MessageResult update(
            @Valid Coin coin,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
            BindingResult bindingResult) {
        Assert.notNull(admin, msService.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));
        notNull(coin.getName(), "validate coin.name!");
        Coin one = coinService.findByCoinId(coin.getName());
        notNull(one, "validate coin.name!");
        coin.setId(one.getId());
        coinService.save(coin);
        return success();
    }

    @GetMapping("get-no-check-key")
    public MessageResult getKey(String phone) {
        String key = SysConstant.ADMIN_COIN_TRANSFER_COLD_PREFIX + phone + "_PASS";
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object object = valueOperations.get(key);
        if (object == null) {
            return error(msService.getMessage("NEED_CODE"));
        }
        return success(msService.getMessage("NO_NEED_CODE"), object);
    }

    /**
     * Create new wallet records for all users for a given coin
     * 1. Use JDBC batch insert
     * 2. By default, wallet addresses are not obtained; users acquire them when recharging
     *
     * @param coinName coin identifier
     * @return MessageResult
     */
    @RequiresPermissions("system:coin:newwallet")
    @RequestMapping("create-member-wallet")
    public MessageResult createCoin(String coinName) {
        Coin coin = coinService.findByCoinId(coinName);
        if (coin == null) {
            return MessageResult.error(msService.getMessage("CURRENCY_CONFIGURATION_NOT_FOUND"));
        }
        List<Member> list = memberFeign.findAllList();
        list.forEach(member -> {
            MemberWallet wallet = memberWalletFeign.findByCoinUnitAndMemberId(coin.getUnit(), member.getId());
            if (wallet == null) {
                MemberWallet wallet1 = new MemberWallet();
                wallet1.setCoinId(coin.getUnit());
                wallet1.setMemberId(member.getId());
                wallet1.setBalance(BigDecimal.ZERO);
                wallet1.setFrozenBalance(BigDecimal.ZERO);
                wallet1.setReleaseBalance(BigDecimal.ZERO);
                wallet1.setVersion(1);
                memberWalletFeign.save(wallet1);
            }
        });
        return MessageResult.success(msService.getMessage("SUCCESS"));
    }

    @PostMapping("all-name-and-unit")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Get all coin names and units")
    public MessageResult getAllCoinNameAndUnit() {
        List<Coin> list = coinService.getAllCoinNameAndUnit();
        return MessageResult.getSuccessInstance(msService.getMessage("SUCCESS"), list);
    }
}
