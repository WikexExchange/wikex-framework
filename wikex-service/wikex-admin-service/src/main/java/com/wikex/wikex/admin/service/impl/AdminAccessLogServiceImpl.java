package com.wikex.wikex.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.entity.AdminAccessLog;
import com.wikex.wikex.admin.mapper.AdminAccessLogMapper;
import com.wikex.wikex.admin.service.AdminAccessLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.vo.AdminAccessLogVo;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.AccessLogScreen;
import org.springframework.stereotype.Service;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
@Service
public class AdminAccessLogServiceImpl extends ServiceImpl<AdminAccessLogMapper, AdminAccessLog> implements AdminAccessLogService {

    @Override
    public IPage<AdminAccessLogVo> pageQuery(AccessLogScreen accessLogScreen) {
        IPage<AdminAccessLogVo> page = new Page<>(accessLogScreen.getPageNo(),accessLogScreen.getPageSize());
        IPage<AdminAccessLogVo> logVoIPage = this.baseMapper.pageQuery(page, accessLogScreen.getAdminName(), accessLogScreen.getModule()==null?null:accessLogScreen.getModule().getCode());
        for (AdminAccessLogVo record : logVoIPage.getRecords()) {
            record.setModuleName(AdminModule.creator(record.getModule()).getDescription());
        }
        return logVoIPage;
    }
}
