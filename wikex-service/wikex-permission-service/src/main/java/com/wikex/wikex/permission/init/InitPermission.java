package com.wikex.wikex.permission.init;

import com.wikex.wikex.permission.entity.Permission;
import com.wikex.wikex.permission.service.PermissionService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InitPermission implements ApplicationRunner {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<Permission> permissionMatch0 = permissionService.findByMatch(0);
        List<Permission> permissionMatch1 = permissionService.findByMatch(1);

        List<Map<Integer, Integer>> rolePermissions = permissionService.allRolePermissions();

        Map<String, Set<Integer>> roleMap = rolePermissionFilter(rolePermissions);

        redisTemplate.boundHashOps("RolePermissionAll").put("PermissionMatch0", permissionMatch0);
        redisTemplate.boundHashOps("RolePermissionAll").put("PermissionMatch1", permissionMatch1);

        redisTemplate.boundHashOps("RolePermissionMap").putAll(roleMap);

    }

    public Map<String, Set<Integer>> rolePermissionFilter(List<Map<Integer, Integer>> rolePermissions) {

        Map<String, Set<Integer>> rolePermissionMapping = new HashMap<>();

        for (Map<Integer, Integer> rolePermissionMap : rolePermissions) {

            Integer rid = rolePermissionMap.get("rid");

            Integer pid = rolePermissionMap.get("pid");

            String key = "Role_" + rid;
            Set<Integer> permissionsSet = rolePermissionMapping.get(key);
            permissionsSet = permissionsSet == null ? new HashSet<>() : permissionsSet;

            permissionsSet.add(pid);
            rolePermissionMapping.put(key, permissionsSet);
        }
        return rolePermissionMapping;
    }
}
