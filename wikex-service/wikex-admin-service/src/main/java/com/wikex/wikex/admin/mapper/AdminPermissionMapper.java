package com.wikex.wikex.admin.mapper;

import com.wikex.wikex.admin.entity.AdminPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Backend Menu Mapper Interface
 * </p>
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminPermissionMapper extends BaseMapper<AdminPermission> {

    List<AdminPermission> getPermissionsByRid(@Param("rid") Long rid);
}
