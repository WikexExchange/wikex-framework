package com.wikex.wikex.admin.service.impl;

import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.mapper.AdminPermissionMapper;
import com.wikex.wikex.admin.service.AdminPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.service.AdminRolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
@Service
public class AdminPermissionServiceImpl extends ServiceImpl<AdminPermissionMapper, AdminPermission> implements AdminPermissionService {

    @Autowired
    private AdminRolePermissionService adminRolePermissionService;

    @Override
    public List<AdminPermission> getPermissionsByRid(Long uid) {
        return this.baseMapper.getPermissionsByRid(uid);
    }

    @Override
    public void deletes(Long[] ids) {
        for (Long id : ids) {
            adminRolePermissionService.deleteByPermissionId(id);
            this.removeById(id);
        }

    }
}
