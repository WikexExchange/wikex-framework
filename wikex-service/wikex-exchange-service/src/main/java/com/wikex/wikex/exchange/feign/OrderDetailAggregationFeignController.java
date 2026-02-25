package com.wikex.wikex.exchange.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exchange.entity.OrderDetailAggregation;
import com.wikex.wikex.exchange.service.OrderDetailAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("orderDetailAggregationFeign")
public class OrderDetailAggregationFeignController extends BaseController {
    @Autowired
    private OrderDetailAggregationService orderDetailAggregationService;

    @RequestMapping("/save")
    public void save(@RequestBody OrderDetailAggregation aggregation){
        orderDetailAggregationService.save(aggregation);
    }


}
