package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.AppRevision;
import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.admin.service.AppRevisionService;
import com.wikex.wikex.admin.vo.AppRevisionCreate;
import com.wikex.wikex.admin.vo.AppRevisionUpdate;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("system/app-revision")
public class AppRevisionController extends BaseAdminController {
    @Autowired
    private AppRevisionService service;

    
    @PostMapping
    public MessageResult create(@Valid AppRevisionCreate model, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        service.save(model.transformation());
        return success();
    }

    
    @PutMapping("{id}")
    public MessageResult put(@PathVariable("id") Long id, AppRevisionUpdate model) {
        AppRevision appRevision = service.getById(id);
        Assert.notNull(appRevision, "validate appRevision id!");
        appRevision = model.transformation(appRevision);
        service.updateById(appRevision);
        return success();
    }

    
    @GetMapping("{id}")
    public MessageResult get(@PathVariable("id") Long id) {
        AppRevision appRevision = service.getById(id);
        Assert.notNull(appRevision, "validate appRevision id!");
        return success(appRevision);
    }

    
    @GetMapping("page-query")
    public MessageResult get(PageParam pageParam) {
        IPage<AppRevision> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        IPage<AppRevision> all = service.page(page);
        return success(IPage2Page(all));
    }
}
