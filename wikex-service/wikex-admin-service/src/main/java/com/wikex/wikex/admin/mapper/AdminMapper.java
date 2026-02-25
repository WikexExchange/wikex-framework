package com.wikex.wikex.admin.mapper;

import com.wikex.wikex.admin.entity.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * Admin Mapper Interface
 * </p>
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminMapper extends BaseMapper<Admin> {

    Map findAdminDetail(@Param("id") Long id);
}
