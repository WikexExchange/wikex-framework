package com.wikex.wikex.robot.market.feign;

import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "wikex-robot-market",contextId = "robot-market")
public interface RobotMarketFeign {

    @RequestMapping("thumb4Feign/{pair}")
    MessageResult findThumb(@PathVariable(value = "pair") String pair);
}
