package com.wikex.wikex.permission.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.permission.entity.Permission;

import java.util.List;
import java.util.Map;

public interface PermissionService extends IService<Permission> {

    
    List<Permission> findByMatch(Integer matchMethod);

    
    List<Map<Integer,Integer>> allRolePermissions();

}
