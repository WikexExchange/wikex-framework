package com.wikex.wikex.admin.controller.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.screen.ConvertCoinScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.ConvertCoin;
import com.wikex.wikex.user.feign.ConvertFeign;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.util.Assert.notNull;


@RestController
@RequestMapping("/convert/coin")
@Slf4j
public class ConvertCoinController extends BaseAdminController {

    @Autowired
    private ConvertFeign convertFeign;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("convert:coin:create")
    @PostMapping("create")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create Convert Coin")
    public MessageResult create(@Valid ConvertCoin convertCoin) {
        ConvertCoin one = convertFeign.findByCoinUnit(convertCoin.getCoinUnit());
        if (one != null) {
            return error(messageSource.getMessage("COIN_NAME_EXIST"));
        }
        convertCoin.setUpdateTime(DateUtil.getCurrentDate());
        convertCoin.setCreateTime(DateUtil.getCurrentDate());
        convertFeign.save(convertCoin);

        return success();
    }

    @RequiresPermissions("convert:coin:update")
    @PostMapping("update")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Update Convert Coin")
    public MessageResult update(
            @Valid ConvertCoin convertCoin,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
            BindingResult bindingResult) {

        notNull(admin, messageSource.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));

        notNull(convertCoin.getCoinUnit(), "validate Coin.Unit!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        ConvertCoin one = convertFeign.findByCoinUnit(convertCoin.getCoinUnit());
        notNull(one, "validate coin.name!");
        convertCoin.setUpdateTime(DateUtil.getCurrentDate());
        convertFeign.save(convertCoin);
        return success();
    }

    @RequiresPermissions("convert:coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Admin Convert Coin Detail")
    public MessageResult detail(@RequestParam("coinUnit") String coinUnit) {
        ConvertCoin convertCoin = convertFeign.findByCoinUnit(coinUnit);
        notNull(convertCoin, "validate Coin.Unit!");
        return success(convertCoin);
    }

    @RequiresPermissions("convert:coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paginated query Convert Coin")
    public MessageResult pageQuery(ConvertCoinScreen screen) {
//        // If pageModel has no property set, default to sorting by createTime descending
//        if (pageModel.getProperty() == null) {
//            List<String> list = new ArrayList<>();
//            list.add("createTime");
//            List<Sort.Direction> directions = new ArrayList<>();
//            directions.add(Sort.Direction.DESC);
//            pageModel.setProperty(list);
//            pageModel.setDirection(directions);
//        }
        Page<ConvertCoin> pageResult = convertFeign.findAll(screen);
        return success(IPage2Page(pageResult));
    }
}
