package com.wikex.wikex.admin.controller.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.ConvertOrderScreen;
import com.wikex.wikex.user.entity.ConvertOrder;
import com.wikex.wikex.user.feign.ConvertFeign;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flash exchange order controller
 */
@RestController
@RequestMapping("/convert/order")
public class ConvertOrderController extends BaseAdminController {
    @Autowired
    private ConvertFeign convertFeign;

    @RequiresPermissions(value = {"convert:order:page-query"})
    @RequestMapping("/page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "Flash Exchange Order List")
    public MessageResult pageQuery(ConvertOrderScreen screen) {
        IPage<ConvertOrder> pageListMapResult = convertFeign.findOrderAll(screen);
        return success(IPage2Page(pageListMapResult));
    }
}
