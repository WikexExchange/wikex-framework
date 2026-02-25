package com.wikex.wikex.exchange.feign;

import com.wikex.wikex.exchange.entity.OrderDetailAggregation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-exchange",contextId = "orderDetailAggregationFeign")
public interface OrderDetailAggregationFeign {

    @RequestMapping("/orderDetailAggregationFeign/save")
    void save(@RequestBody OrderDetailAggregation aggregation);
}
