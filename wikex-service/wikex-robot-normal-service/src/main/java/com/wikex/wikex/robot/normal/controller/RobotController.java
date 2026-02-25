package com.wikex.wikex.robot.normal.controller;

import com.wikex.wikex.robot.market.feign.RobotMarketFeign;
import com.wikex.wikex.robot.normal.entity.CustomRobotKline;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.robot.normal.robot.ExchangeRobot;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotCustom;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotFactory;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotNormal;
import com.wikex.wikex.robot.normal.service.CustomRobotKlineService;
import com.wikex.wikex.robot.normal.service.RobotParamService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RobotController {

	private final static  Logger logger  =  LoggerFactory.getLogger(RobotController.class);

	@Autowired
	private ExchangeRobotFactory exchangeRobotFactory;
	@Autowired
	private LocaleMessageSourceService messageSource;

	@RequestMapping("robotList")
	public MessageResult robotList(){
		Map<String, RobotParams> retMap = new HashMap<String, RobotParams>();
		Map<String, ExchangeRobot> robotList = exchangeRobotFactory.getRobotList();
		for (Map.Entry<String, ExchangeRobot> entry : robotList.entrySet()) {
			retMap.put(entry.getKey(), entry.getValue().getRobotParams());
		}
		MessageResult mr = new MessageResult(0,messageSource.getMessage("GET_SUCCESS"));
		mr.setData(retMap);
		return mr;
    }


	
	@RequestMapping("startRobot")
	public MessageResult startRobot(String coinName) {
		if(exchangeRobotFactory.containsExchangeRobot(coinName)) {
			ExchangeRobot robot = exchangeRobotFactory.getExchangeRobot(coinName);
			robot.startRobot();
			MessageResult mr = new MessageResult(0,messageSource.getMessage("ROBOT_START_SUCCESS"));
			return mr;
		}else {
			MessageResult mr = new MessageResult(500, messageSource.getMessage("START_FAILED_ROBOT_NOT_FOUND"));
			return mr;
		}
	}

	
	@RequestMapping("stopRobot")
	public MessageResult stopRobot(String coinName) {
		if(exchangeRobotFactory.containsExchangeRobot(coinName)) {
			ExchangeRobot robot = exchangeRobotFactory.getExchangeRobot(coinName);
			robot.stopRobot();
			MessageResult mr = new MessageResult(0,messageSource.getMessage("ROBOT_STOP_SUCCESS"));
			return mr;
		}else {
			MessageResult mr = new MessageResult(500, messageSource.getMessage("STOP_FAILED_ROBOT_NOT_FOUND"));
			return mr;
		}
	}
}
