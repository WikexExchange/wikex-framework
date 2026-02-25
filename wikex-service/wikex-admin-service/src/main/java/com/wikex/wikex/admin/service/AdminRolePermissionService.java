package com.wikex.wikex.admin.service;

import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.entity.AdminRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminRolePermissionService extends IService<AdminRolePermission> {

    List<AdminPermission> getPermissionsByRoleId(Long roleId);

    void deleteByPermissionId(Long id);

    void deleteByRoleId(Long id);
}
