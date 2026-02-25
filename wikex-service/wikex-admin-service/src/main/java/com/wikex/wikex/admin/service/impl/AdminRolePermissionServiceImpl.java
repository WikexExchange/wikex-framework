package com.wikex.wikex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.entity.AdminRolePermission;
import com.wikex.wikex.admin.mapper.AdminRolePermissionMapper;
import com.wikex.wikex.admin.service.AdminRolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
@Service
public class AdminRolePermissionServiceImpl extends ServiceImpl<AdminRolePermissionMapper, AdminRolePermission> implements AdminRolePermissionService {

    @Override
    public List<AdminPermission> getPermissionsByRoleId(Long roleId) {
        return this.baseMapper.getPermissionsByRoleId(roleId);
    }

    @Override
    public void deleteByPermissionId(Long id) {
        QueryWrapper<AdminRolePermission> query = new QueryWrapper<>();
        query.eq("rule_id",id);
        this.remove(query);
    }

    @Override
    public void deleteByRoleId(Long id) {
        QueryWrapper<AdminRolePermission> query = new QueryWrapper<>();
        query.eq("role_id",id);
        this.remove(query);
    }
}
