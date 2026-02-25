package com.wikex.wikex.admin.controller.p2p;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.OrderStatus;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.feign.OtcOrderFeign;
import com.wikex.wikex.screen.OrderScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.ExcelUtil;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.OtcOrderVO;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author Hevin
 * @description Fiat currency trading orders
 * @date 2019/1/8 15:41
 */
@RestController
@RequestMapping("/otc/order")
public class AdminOrderController extends BaseController {

    @Autowired
    private OtcOrderFeign orderService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("otc:order:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.OTC, operation = "All fiat trading orders (Order)")
    public MessageResult all() {
        List<OtcOrder> exchangeOrderList = orderService.findAll();
        if (exchangeOrderList != null && exchangeOrderList.size() > 0) {
            return success(exchangeOrderList);
        }
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("otc:order:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.OTC, operation = "Fiat trading order (Order) details")
    public MessageResult detail(Long id) {
        OtcOrder one = orderService.findOne(id);
        if (one == null) {
            return error(messageSource.getMessage("NO_DATA"));
        }
        return success(one);
    }

    // Modify order status
    @RequiresPermissions("otc:order:alert-status")
    @PatchMapping("{id}/alert-status")
    @AccessLog(module = AdminModule.OTC, operation = "Modify fiat trading order (Order)")
    public MessageResult status(
            @PathVariable("id") Long id,
            @RequestParam("status") OrderStatus status) {
        OtcOrder order = orderService.findOne(id);
        notNull(order, "validate order.id!");
        order.setStatus(status);
        orderService.updateById(order);
        return success();
    }

    @RequiresPermissions(value = {"otc:order:page-query","finance:otc:order:page-query"},logical = Logical.OR)
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.OTC, operation = "Paginated search for fiat trading orders (Order)")
    public MessageResult page(OrderScreen screen) {
        Page<OtcOrderVO> page = orderService.outExcel(screen);
        return success(IPage2Page(page));
    }

    @RequiresPermissions("otc:order:get-order-num")
    @PostMapping("get-order-num")
    @AccessLog(module = AdminModule.OTC, operation = "Admin homepage - total number of orders API")
    public MessageResult getOrderNum() {
        return orderService.getOrderNum();
    }

    /**
     * Parameter `fileName` is the file name of the exported Excel file with format `.xls`. 
     * Defined in `OutExcelInterceptor` interceptor. It is not a required parameter.
     *
     * @param screen   Query conditions
     * @param response HTTP response for output
     * @throws Exception Exception during export
     */
    @RequiresPermissions("otc:order:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "Export fiat trading orders (Order) to Excel")
    public void outExcel(
            OrderScreen screen,
            HttpServletResponse response
    ) throws Exception {
        List<OtcOrderVO> list = orderService.outExcel(screen).getRecords();
        ExcelUtil.listToExcel(list,OtcOrderVO.class.getDeclaredFields(),response.getOutputStream());
    }

}
