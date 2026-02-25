package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.entity.AdminRole;
import com.wikex.wikex.admin.entity.AdminRolePermission;
import com.wikex.wikex.admin.service.AdminPermissionService;
import com.wikex.wikex.admin.service.AdminRolePermissionService;
import com.wikex.wikex.admin.service.AdminRoleService;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.core.Menu;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hevin
 * @date 2020-12-19
 */
@RestController
@RequestMapping(value = "system/role")
public class RoleController extends BaseAdminController {

    @Autowired
    private AdminRoleService adminRoleService;
    @Autowired
    private AdminPermissionService adminPermissionService;
    @Autowired
    private AdminRolePermissionService adminRolePermissionService;

    /**
     * Create or update role
     *
     * @param sysRole
     * @param bindingResult
     * @return
     */
    @RequiresPermissions("system:role:merge")
    @RequestMapping("merge")
    @Transactional(rollbackFor = Exception.class)
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create or update role SysRole")
    public MessageResult mergeRole(@Valid AdminRole sysRole, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        adminRoleService.saveOrUpdate(sysRole);
        if (sysRole != null) {
            // Update permissions
            // Delete previous ones
            adminRolePermissionService.deleteByRoleId(sysRole.getId());
            // Create new ones
            List<AdminRolePermission> collect = Arrays.stream(sysRole.getPermissions()).map(x -> {
                AdminRolePermission arp = new AdminRolePermission();
                arp.setRoleId(sysRole.getId());
                arp.setRuleId(x);
                return arp;
            }).collect(Collectors.toList());
            adminRolePermissionService.saveBatch(collect);
            result = success("Operation successful");
            result.setData(sysRole);
            return result;
        } else {
            return MessageResult.error(500, "Operation failed");
        }
    }

    /**
     * Get full permission tree
     *
     * @return
     */
    @RequiresPermissions("system:role:permission:all")
    @RequestMapping("permission/all")
    @AccessLog(module = AdminModule.SYSTEM, operation = "All permission tree Menu")
    public MessageResult allMenu() {
        List<Menu> list = adminRoleService.toMenus(adminPermissionService.list(), 0L);
        MessageResult result = success("success");
        result.setData(list);
        return result;
    }

    /**
     * Get permissions owned by role
     *
     * @param roleId
     * @return
     */
    @RequiresPermissions("system:role:permission")
    @RequestMapping("permission")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Permissions owned by role Menu")
    public MessageResult roleAllPermission(Long roleId) {
        List<AdminPermission> list = adminRolePermissionService.getPermissionsByRoleId(roleId);
        List<Menu> content = adminRoleService.toMenus(list, 0L);
        MessageResult result = success();
        result.setData(content);
        return result;
    }

    /**
     * Get all roles
     *
     * @return
     */
    @RequiresPermissions("system:role:all")
    @RequestMapping("all")
    @AccessLog(module = AdminModule.SYSTEM, operation = "All roles SysRole")
    public MessageResult getAllRole(PageParam pageParam) {
        IPage<AdminRole> all = adminRoleService.findAll(pageParam.getPageNo(), pageParam.getPageSize());
        return success(IPage2Page(all));
    }

    /**
     * Delete role
     *
     * @return
     */
    @RequiresPermissions("system:role:deletes")
    @RequestMapping("deletes")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Delete role SysRole")
    public MessageResult deletes(Long id) {
        MessageResult result = adminRoleService.deletes(id);
        if (result.getCode() == 0) {
            // Delete related permissions
            adminRolePermissionService.deleteByRoleId(id);
        }
        return result;
    }
}
