package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.AdminAccessLog;
import com.wikex.wikex.admin.service.AdminAccessLogService;
import com.wikex.wikex.admin.service.AdminService;
import com.wikex.wikex.admin.vo.AdminAccessLogVo;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.AccessLogScreen;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.util.Assert.notNull;

 // Log Management
@Slf4j
@RestController
@RequestMapping("/system/access-log")
@Transactional(readOnly = true)
public class AccessLogController extends BaseAdminController {

    @Autowired
    private AdminAccessLogService adminAccessLogService;

    @Autowired
    private AdminService adminService ;

    /**
     * Get all access logs
     */
    @RequiresPermissions("system:access-log:all")
    @GetMapping("/all")
    @AccessLog(module = AdminModule.SYSTEM, operation = "All operation/access logs - AdminAccessLog")
    public MessageResult all() {
        List<AdminAccessLog> adminAccessLogList = adminAccessLogService.list();
        return success(adminAccessLogList);
    }

    /**
     * Get details of an access log by ID
     */
    @RequiresPermissions("system:access-log:detail")
    @GetMapping("/{id}")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Operation/access log detail - AdminAccessLog")
    public MessageResult detail(@PathVariable("id") Long id) {
        AdminAccessLog adminAccessLog = adminAccessLogService.getById(id);
        notNull(adminAccessLog, "validate id!");
        return success(adminAccessLog);
    }

    /**
     * Paginated query for access logs
     */
    @RequiresPermissions("system:access-log:page-query")
    @GetMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paginated query operation/access logs - AdminAccessLog")
    public MessageResult pageQuery(AccessLogScreen accessLogScreen) {
        IPage<AdminAccessLogVo> all = adminAccessLogService.pageQuery(accessLogScreen);
        return success(IPage2Page(all));
    }

}
