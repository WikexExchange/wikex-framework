package com.wikex.wikex.robot.normal.feign;

import com.wikex.wikex.robot.normal.entity.CustomRobotKline;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(value = "wikex-robot-normal",contextId = "robot-normal")
public interface RobotNormalFeign {

    @RequestMapping("getRobotParams")
    MessageResult<RobotParams> getRobotParams(@RequestParam("coinName") String coinName);

    @RequestMapping("setRobotParams")
    MessageResult setRobotParams(@RequestBody RobotParams params);

    @RequestMapping("createRobot")
    MessageResult createRobot(@RequestBody RobotParams params);

    @RequestMapping("createCustomRobot")
    MessageResult createCustomRobot(@RequestBody RobotParams params);

    @RequestMapping("setRobotStrategy")
    MessageResult setRobotStrategy(@RequestParam("coinName") String coinName, @RequestParam("strategy")Integer strategy, @RequestParam("flowPair")String flowPair, @RequestParam("flowPercent")BigDecimal flowPercent);

    @RequestMapping("saveKline")
    MessageResult saveKline(@RequestBody CustomRobotKline params);

    @RequestMapping("getRobotKline")
    MessageResult getRobotKline(@RequestParam("coinName") String coinName, @RequestParam("kdate")String kdate);
}
