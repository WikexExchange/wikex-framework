package com.wikex.wikex.admin.controller.option;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.feign.ContractOptionFeign;
import com.wikex.wikex.screen.ContractOptionScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/option")
@Slf4j
public class ContractOptionController extends BaseAdminController {
    @Autowired
    private ContractOptionFeign contractOptionService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Query option contracts
     * @param screen query parameters
     * @return
     */
    @RequiresPermissions("option:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Option contract trading pair list")
    public MessageResult detail(
            ContractOptionScreen screen) {
        // Get query conditions
        Page<ContractOption> all = contractOptionService.findAll(screen);
        return success(IPage2Page(all));
    }

    /**
     * Modify preset price
     * @param presetPrice Preset price
     * @return
     */
    @RequiresPermissions("option:alter")
    @PostMapping("alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Option contract modify preset price")
    public MessageResult alter(
            @RequestParam(value = "id",required = true) Long id,
            @RequestParam(value = "presetPrice", required = true) BigDecimal presetPrice // Preset price to allow betting
    ) {
        ContractOption option = contractOptionService.findOne(id);
        if(option == null) {
            return error(messageSource.getMessage("OPTIONS_CONTRACT") + id + messageSource.getMessage("NOT_FOUND"));
        }
        if(presetPrice != null) option.setPresetPrice(presetPrice);
        contractOptionService.alert(option);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }
}
