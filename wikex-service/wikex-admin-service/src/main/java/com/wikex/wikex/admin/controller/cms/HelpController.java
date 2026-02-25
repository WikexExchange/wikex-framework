package com.wikex.wikex.admin.controller.cms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.SysHelp;
import com.wikex.wikex.admin.service.SysHelpService;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.FileUtil;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.Validate.notNull;

 // Admin Help Web
@RestController
@RequestMapping("/cms/system-help")
public class HelpController extends BaseAdminController {

    @Autowired
    private SysHelpService sysHelpService;
    @Autowired
    private LocaleMessageSourceService msService;

    @RequiresPermissions("cms:system-help:create")
    @PostMapping("/create")
    @AccessLog(module = AdminModule.CMS, operation = "Create system help")
    public MessageResult create(@Valid SysHelp sysHelp, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        sysHelp.setCreateTime(DateUtil.getCurrentDate());
        sysHelpService.save(sysHelp);
        return success(sysHelp);
    }

    @RequiresPermissions("cms:system-help:all")
    @PostMapping("/all")
    @AccessLog(module = AdminModule.CMS, operation = "Find all system help")
    public MessageResult all() {
        List<SysHelp> sysHelps = sysHelpService.list();
        if (sysHelps != null && sysHelps.size() > 0) {
            return success(sysHelps);
        }
        return error("data null");
    }

    @RequiresPermissions("cms:system-help:top")
    @PostMapping("top")
    @AccessLog(module = AdminModule.CMS, operation = "Pin system help to top")
    public MessageResult toTop(@RequestParam("id") long id) {
        SysHelp help = sysHelpService.getById(id);
        int a = sysHelpService.getMaxSort();
        help.setSort(a + 1);
        help.setIsTop("0");
        sysHelpService.updateById(help);
        return success(msService.getMessage("TOP_SUCCESS"));
    }

    /**
     * Unpin system help
     * @param id id
     * @return result
     */
    @RequiresPermissions("cms:system-help:down")
    @PostMapping("down")
    @AccessLog(module = AdminModule.CMS, operation = "Unpin system help")
    public MessageResult toDown(@RequestParam("id") long id) {
        SysHelp help = sysHelpService.getById(id);
        help.setIsTop("1");
        sysHelpService.updateById(help);
        return success();
    }

    @RequiresPermissions("cms:system-help:detail")
    @PostMapping("/detail")
    @AccessLog(module = AdminModule.CMS, operation = "System help detail")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        SysHelp sysHelp = sysHelpService.getById(id);
        notNull(sysHelp, "validate id!");
        return success(sysHelp);
    }

    @RequiresPermissions("cms:system-help:update")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.CMS, operation = "Update system help")
    public MessageResult update(@Valid SysHelp sysHelp, BindingResult bindingResult) {
        notNull(sysHelp.getId(), "validate id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        SysHelp one = sysHelpService.getById(sysHelp.getId());
        notNull(one, "validate id!");
        sysHelpService.updateById(sysHelp);
        return success();
    }

    @RequiresPermissions("cms:system-help:deletes")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.CMS, operation = "Delete system help")
    public MessageResult deleteOne(@RequestParam("ids") Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        sysHelpService.removeByIds(idList);
        return success();
    }

    @RequiresPermissions("cms:system-help:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.CMS, operation = "Paginated query system help")
    public MessageResult pageQuery(PageParam pageParam) {
        IPage<SysHelp> page = new Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        IPage<SysHelp> all = sysHelpService.page(page);
        return success(IPage2Page(all));
    }

    @RequiresPermissions("cms:system-help:out-excel")
    @GetMapping("/out-excel")
    @AccessLog(module = AdminModule.CMS, operation = "Export system help Excel")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = sysHelpService.list();
        return new FileUtil().exportExcel(request, response, all, "sysHelp");
    }
}
