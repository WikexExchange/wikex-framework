package com.wikex.wikex.admin.service;

import com.wikex.wikex.admin.entity.AdminPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminPermissionService extends IService<AdminPermission> {

    List<AdminPermission> getPermissionsByRid(Long uid);

    void deletes(Long[] ids);
}
