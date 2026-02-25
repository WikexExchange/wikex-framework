package com.wikex.wikex.admin.mapper;

import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.entity.AdminRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Backend User Permission Mapper Interface
 * </p>
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminRolePermissionMapper extends BaseMapper<AdminRolePermission> {

    List<AdminPermission> getPermissionsByRoleId(@Param("roleId") Long roleId);
}
