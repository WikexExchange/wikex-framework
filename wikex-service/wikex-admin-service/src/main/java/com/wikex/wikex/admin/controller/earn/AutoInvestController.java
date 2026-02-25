package com.wikex.wikex.admin.controller.earn;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.earn.entity.AutoInvestActivity;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.wikex.wikex.earn.feign.AutoInvestActivityFeign;
import com.wikex.wikex.earn.feign.AutoInvestPlanFeign;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.apache.commons.lang3.Validate.notNull;

 // Admin - Auto Investment (Earn) endpoints
@RestController
@RequestMapping("/auto/invest")
@Slf4j
public class AutoInvestController extends BaseAdminController {

    @Autowired
    private AutoInvestActivityFeign autoInvestActivityService;
    @Autowired
    private AutoInvestPlanFeign autoInvestPlanService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("auto:invest:create")
    @PostMapping("create")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create auto-invest activity")
    public MessageResult create(@Valid AutoInvestActivity activity) {
        notNull(activity.getCoinUnit(), "validate Coin.Unit!");
        activity.setUpdateTime(DateUtil.getCurrentDate());
        activity.setCreateTime(DateUtil.getCurrentDate());
        autoInvestActivityService.save(activity);
        return success();
    }

    @RequiresPermissions("auto:invest:update")
    @PostMapping("update")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Update auto-invest activity")
    public MessageResult update(
            @Valid AutoInvestActivity activity,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
            BindingResult bindingResult) {

        Assert.notNull(admin, messageSource.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));

        notNull(activity.getCoinUnit(), "validate Coin.Unit!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        AutoInvestActivity one = autoInvestActivityService.findById(activity.getId());
        notNull(one, "validate coin.name!");
        activity.setUpdateTime(DateUtil.getCurrentDate());
        autoInvestActivityService.save(activity);
        return success();
    }

    @RequiresPermissions("auto:invest:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Auto-invest activity detail (admin)")
    public MessageResult detail(@RequestParam("id") Long id) {
        AutoInvestActivity one = autoInvestActivityService.findById(id);
        notNull(one, "validate Coin.Unit!");
        return success(one);
    }

    @RequiresPermissions("auto:invest:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paged query of auto-invest activities")
    public MessageResult pageQuery(ActivityParam pageParam) {
        Page<AutoInvestActivity> pageResult = autoInvestActivityService.findAll(pageParam);
        return success(IPage2Page(pageResult));
    }

    @RequiresPermissions("auto:plan:page-query")
    @PostMapping("queryPlan")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paged query of auto-invest plans")
    public MessageResult queryPlan(ActivityParam pageParam) {
        Page<AutoInvestPlan> pageResult = autoInvestPlanService.findAll(pageParam);
        return success(IPage2Page(pageResult));
    }
}
