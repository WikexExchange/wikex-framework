package com.wikex.wikex.admin.controller.system;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.admin.service.DataDictionaryService;
import com.wikex.wikex.admin.vo.DataDictionaryCreate;
import com.wikex.wikex.admin.vo.DataDictionaryUpdate;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Date;



@Slf4j
@RestController
@RequestMapping("system/data-dictionary")
public class DataDictionaryController extends BaseAdminController {
    @Autowired
    private DataDictionaryService service;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @PostMapping
    public MessageResult post(@Valid DataDictionaryCreate model, BindingResult bindingResult) {
        
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        DataDictionary data = service.findByBond(model.getBond());
        if (data != null) {
            return error("bond already existed!");
        }
        DataDictionary dictionary = model.transformation();
        dictionary.setCreationTime(new Date());
        dictionary.setUpdateTime(new Date());
        service.saveOrUpdate(dictionary);
        rocketMQTemplate.convertAndSend("data-dictionary-save-update", JSON.toJSONString(model.transformation()));
        
        return success();
    }

    @GetMapping
    public MessageResult page(PageParam pageParam) {
        IPage<DataDictionary> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        IPage<DataDictionary> all = service.page(page);
        return success(IPage2Page(all));
    }
    @RequiresPermissions("system:data-dictionary:update")
    @PutMapping("/{bond}")
    public MessageResult put(@PathVariable("bond") String bond, DataDictionaryUpdate model) {
        
        DataDictionary dataDictionary = service.findByBond(bond);
        Assert.notNull(dataDictionary, "validate bond");
        dataDictionary = model.transformation(dataDictionary);
        dataDictionary.setUpdateTime(new Date());
        service.updateById(dataDictionary);
        dataDictionary.setValue(model.getValue());
        dataDictionary.setComment(model.getComment());
        rocketMQTemplate.convertAndSend("data-dictionary-save-update",JSON.toJSONString(dataDictionary));
        return success();
    }


}
