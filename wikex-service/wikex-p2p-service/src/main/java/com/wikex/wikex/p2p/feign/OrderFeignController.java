package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.event.OrderEvent;
import com.wikex.wikex.p2p.service.OtcOrderService;
import com.wikex.wikex.screen.OrderScreen;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.OtcOrderVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping(value = "/orderFeign", method = RequestMethod.POST)
@Slf4j
public class OrderFeignController extends BaseController {

    @Autowired
    private OtcOrderService orderService;
    @Autowired
    private OrderEvent orderEvent;


    @PostMapping(value = "findOne")
    public OtcOrder findOne(@RequestParam("id") Long id){
        return orderService.getById(id);
    }

    @PostMapping(value = "findOneByOrderSn")
    public OtcOrder findOneByOrderSn(@RequestParam("orderSn")String orderSn){
        return orderService.findOneByOrderSn(orderSn);
    }

    @PostMapping(value = "cancelOrder")
    public Integer cancelOrder(@RequestParam("orderSn")String orderSn){
        return orderService.cancelOrder(orderSn);
    }

    @PostMapping(value = "releaseOrder")
    Integer releaseOrder(@RequestParam("orderSn")String orderSn){
        return orderService.releaseOrder(orderSn);
    }

    @PostMapping(value = "onOrderCompleted")
    public MessageResult onOrderCompleted(@RequestBody OtcOrder order){
        orderEvent.onOrderCompleted(order);
        return success();
    }

    @PostMapping(value = "findAll")
    public List<OtcOrder> findAll(){
        return orderService.list();
    }

    @PostMapping(value = "updateById")
    public MessageResult updateById(@RequestBody OtcOrder order){
        orderService.updateById(order);
        return success();
    }

    @PostMapping(value = "outExcel")
    public Page<OtcOrderVO> outExcel(@RequestBody OrderScreen screen){
        return orderService.outExcel(screen);
    }

    @PostMapping(value = "getOrderNum")
    public MessageResult getOrderNum(){
        return orderService.getOrderNum();
    }

    @PostMapping(value = "getOtcOrderStatistics")
    public List<OtcOrderVO> getOtcOrderStatistics(@RequestParam("dateStr")String dateStr){
        return orderService.getOtcOrderStatistics(dateStr);
    }

}
