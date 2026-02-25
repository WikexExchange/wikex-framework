package com.wikex.wikex.admin.controller.earn;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.earn.entity.LockedSavingsActivity;
import com.wikex.wikex.earn.entity.LockedSavingsOrder;
import com.wikex.wikex.earn.feign.LockedSavingsActivityFeign;
import com.wikex.wikex.earn.feign.LockedSavingsOrderFeign;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.apache.commons.lang3.Validate.notNull;

 // Admin - Locked Savings endpoints
@RestController
@RequestMapping("/locked/activity")
public class LockedSavingsController extends BaseAdminController {

    @Autowired
    private LockedSavingsActivityFeign lockedSavingsActivityService;

    @Autowired
    private LockedSavingsOrderFeign lockedSavingsOrderService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("locked:activity:create")
    @PostMapping("create")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create fixed-term activity")
    public MessageResult create(@Valid LockedSavingsActivity activity) {
        notNull(activity.getCoinUnit(), "validate Coin.Unit!");
        activity.setUpdateTime(DateUtil.getCurrentDate());
        activity.setCreateTime(DateUtil.getCurrentDate());
        lockedSavingsActivityService.save(activity);
        return success();
    }

    @RequiresPermissions("locked:activity:update")
    @PostMapping("update")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Update fixed-term activity")
    public MessageResult update(
            @Valid LockedSavingsActivity activity,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
            BindingResult bindingResult) {

        Assert.notNull(admin, messageSource.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));

        notNull(activity.getCoinUnit(), "validate Coin.Unit!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        LockedSavingsActivity one = lockedSavingsActivityService.findById(activity.getId());
        notNull(one, "validate coin.name!");
        activity.setUpdateTime(DateUtil.getCurrentDate());
        lockedSavingsActivityService.save(activity);
        return success();
    }

    @RequiresPermissions("locked:activity:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Locked savings activity detail (admin)")
    public MessageResult detail(@RequestParam("id") Long id) {
        LockedSavingsActivity one = lockedSavingsActivityService.findById(id);
        notNull(one, "validate Coin.Unit!");
        return success(one);
    }

    @RequiresPermissions("locked:activity:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paged query of fixed-term activities")
    public MessageResult pageQuery(ActivityParam pageParam) {
        Page<LockedSavingsActivity> pageResult = lockedSavingsActivityService.findAll(pageParam);
        return success(IPage2Page(pageResult));
    }

    @RequiresPermissions("locked:order:page-query")
    @PostMapping("queryOrder")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paged query of fixed-term orders")
    public MessageResult queryOrder(ActivityParam pageParam) {
        Page<LockedSavingsOrder> pageResult = lockedSavingsOrderService.findAll(pageParam);
        return success(pageResult);
    }

}
