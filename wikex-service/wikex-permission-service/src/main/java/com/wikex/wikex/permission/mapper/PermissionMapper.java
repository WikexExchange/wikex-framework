package com.wikex.wikex.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.permission.entity.Permission;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface PermissionMapper extends BaseMapper<Permission> {

    
    @Select("SELECT * FROM role_permission")
    List<Map<Integer,Integer>> allRolePermissions();
}
