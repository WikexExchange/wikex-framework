package com.wikex.wikex.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.permission.entity.Permission;
import com.wikex.wikex.permission.mapper.PermissionMapper;
import com.wikex.wikex.permission.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public List<Permission> findByMatch(Integer matchMethod) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<Permission>();
        queryWrapper.eq("url_match",matchMethod);
        return permissionMapper.selectList(queryWrapper);
    }

    @Override
    public List<Map<Integer, Integer>> allRolePermissions() {
        return permissionMapper.allRolePermissions();
    }
}
