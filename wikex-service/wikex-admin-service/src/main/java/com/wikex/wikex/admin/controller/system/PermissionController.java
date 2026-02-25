package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.service.AdminPermissionService;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PermissionScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping("/system/permission")
public class PermissionController extends BaseController {

    @Autowired
    private AdminPermissionService adminPermissionService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("system:permission:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create/Update Permission")
    public MessageResult merge(@Valid AdminPermission permission) {
        adminPermissionService.saveOrUpdate(permission);
        MessageResult result = success(messageSource.getMessage("SAVE_PERMISSION_SUCCESS"));
        result.setData(permission);
        return result;
    }


    @RequiresPermissions("system:permission:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paginated Query Permissions")
    public MessageResult pageQuery(PermissionScreen permissionScreen) {
        IPage<AdminPermission> page = new Page<>(permissionScreen.getPageNo(),permissionScreen.getPageSize());
        QueryWrapper<AdminPermission> query = new QueryWrapper<>();
        query.eq(permissionScreen.getParentId()!=null,"parent_id",permissionScreen.getParentId());
        IPage<AdminPermission> list = adminPermissionService.page(page, query);
        return success(IPage2Page(list));
    }

    @RequiresPermissions("system:permission:detail")
    @PostMapping("/detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Permission Details")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        AdminPermission sysPermission = adminPermissionService.getById(id);
        Assert.notNull(sysPermission, "The permission does not exist");
        return MessageResult.getSuccessInstance(messageSource.getMessage("QUERY_PERMISSION_SUCCESS"), sysPermission);
    }

    @RequiresPermissions("system:permission:deletes")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Batch Delete Permissions")
    public MessageResult deletes(@RequestParam(value = "ids") Long[] ids) {
        adminPermissionService.deletes(ids);
        return MessageResult.success(messageSource.getMessage("BATCH_DELETE_PERMISSION_SUCCESS"));
    }

}
