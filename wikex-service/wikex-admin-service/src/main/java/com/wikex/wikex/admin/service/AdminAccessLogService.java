package com.wikex.wikex.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.entity.AdminAccessLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.admin.vo.AdminAccessLogVo;
import com.wikex.wikex.screen.AccessLogScreen;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminAccessLogService extends IService<AdminAccessLog> {

    IPage<AdminAccessLogVo> pageQuery(AccessLogScreen accessLogScreen);
}
