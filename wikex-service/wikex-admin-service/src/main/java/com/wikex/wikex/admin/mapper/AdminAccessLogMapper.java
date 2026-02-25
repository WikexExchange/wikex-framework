package com.wikex.wikex.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.entity.AdminAccessLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.admin.vo.AdminAccessLogVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Admin Access Log Mapper Interface
 * </p>
 *
 * Provides methods to interact with the admin access log table,
 * including custom pagination query.
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminAccessLogMapper extends BaseMapper<AdminAccessLog> {

    /**
     * Paginated query for admin access logs.
     *
     * @param page      pagination object
     * @param adminName filter by admin name
     * @param module    filter by module
     * @return paginated result of AdminAccessLogVo
     */
    IPage<AdminAccessLogVo> pageQuery(IPage<AdminAccessLogVo> page,
                                      @Param("adminName") String adminName,
                                      @Param("module") Integer module);
}
