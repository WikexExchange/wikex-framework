package com.wikex.wikex.robot.normal.feign;

import com.wikex.wikex.robot.market.feign.RobotMarketFeign;
import com.wikex.wikex.robot.normal.entity.CustomRobotKline;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.robot.normal.robot.ExchangeRobot;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotCustom;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotFactory;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotNormal;
import com.wikex.wikex.robot.normal.service.CustomRobotKlineService;
import com.wikex.wikex.robot.normal.service.RobotParamService;
import com.wikex.wikex.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class RobotFeignController {

	private final static Logger logger = LoggerFactory.getLogger(RobotFeignController.class);

	@Autowired
	private RobotMarketFeign robotMarketFeign;
	@Autowired
	private RobotParamService robotParamService;
	@Autowired
	private CustomRobotKlineService customRobotKlineService;

	@Autowired
	private ExchangeRobotFactory exchangeRobotFactory;

	/**
	 * Set robot parameters
	 * 
	 * @return
	 */
	@RequestMapping("setRobotParams")
	public MessageResult setRobotParams(@RequestBody RobotParams params) {
		logger.info("Set robot parameters: " + params.getCoinName());
		if (exchangeRobotFactory.containsExchangeRobot(params.getCoinName())) {
			exchangeRobotFactory.setRobotParams(params.getCoinName(), params);
			MessageResult mr = new MessageResult(0, "Parameters set successfully");
			return mr;
		} else {
			MessageResult mr = new MessageResult(500, "Robot does not exist");
			return mr;
		}
	}

	/**
	 * Control robot: create/save K-line trend data
	 * 
	 * @return
	 */
	@RequestMapping("saveKline")
	public MessageResult saveKline(@RequestBody CustomRobotKline params) {
		logger.info("Set robot K-line parameters: " + params.getCoinName());
		customRobotKlineService.update(params.getCoinName(), params.getKdate(), params);

		// When saving K-line data, reload today's K-line data for the robot
		if (exchangeRobotFactory.containsExchangeRobot(params.getCoinName())) {
			ExchangeRobot robot = exchangeRobotFactory.getExchangeRobot(params.getCoinName());
			robot.reloadCustomRobotKline(); // Reload robot K-line immediately after modification
		}

		MessageResult mr = new MessageResult(0, "Parameters set successfully");
		return mr;
	}

	/**
	 * Get control robot K-line settings list
	 * 
	 * @param coinName
	 * @param kdate
	 * @return
	 */
	@RequestMapping("getRobotKline")
	public MessageResult getRobotKline(@RequestParam("coinName") String coinName, @RequestParam("kdate") String kdate) {
		logger.info("Get robot K-line parameters: " + coinName);
		List<CustomRobotKline> list = customRobotKlineService.queryRobotKline(coinName, kdate);

		MessageResult mr = new MessageResult(0, "Parameters retrieved successfully");
		mr.setData(list);
		return mr;
	}

	/**
	 * Set control robot strategy
	 * 
	 * @param coinName
	 * @param strategy
	 * @param flowPair
	 * @param flowPercent
	 * @return
	 */
	@RequestMapping("setRobotStrategy")
	public MessageResult setRobotStrategy(String coinName, Integer strategy, String flowPair, BigDecimal flowPercent) {
		logger.info("Set control robot strategy: {} - {} - {} - {}", coinName, strategy, flowPair, flowPercent);
		if (exchangeRobotFactory.containsExchangeRobot(coinName)) {
			exchangeRobotFactory.setRobotStrategy(coinName, strategy, flowPair, flowPercent);
			MessageResult mr = new MessageResult(0, "Robot strategy set successfully");
			return mr;
		} else {
			MessageResult mr = new MessageResult(500, "Robot does not exist");
			return mr;
		}
	}

	/**
	 * Get robot parameters
	 * 
	 * @param coinName
	 * @return
	 */
	@RequestMapping("getRobotParams")
	public MessageResult<RobotParams> getRobotParams(@RequestParam("coinName") String coinName) {
		logger.info("Get robot parameters: " + coinName);
		if (exchangeRobotFactory.containsExchangeRobot(coinName)) {
			MessageResult mr = new MessageResult(0, "Robot parameters retrieved successfully");
			mr.setData(exchangeRobotFactory.getRobotParams(coinName));
			return mr;
		} else {
			MessageResult mr = new MessageResult(500, "Robot does not exist");
			return mr;
		}
	}

	/**
	 * Create normal robot
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping("createRobot")
	public MessageResult createRobot(@RequestBody RobotParams params) {
		logger.info("Create robot: " + params.getCoinName());
		if (exchangeRobotFactory.containsExchangeRobot(params.getCoinName())) {
			MessageResult mr = new MessageResult(500, "Creation failed, robot already exists");
			return mr;
		} else {
			ExchangeRobot robot = new ExchangeRobotNormal();

			robot.setRobotParamSevice(robotParamService);
			robot.setRobotMarketFeign(robotMarketFeign);

			robot.setRobotParams(params);
			robot.setPlateSymbol(params.getCoinName());

			exchangeRobotFactory.addExchangeRobot(params.getCoinName(), robot);

			new Thread((ExchangeRobotNormal) robot).start();

			MessageResult mr = new MessageResult(0, "Robot created successfully");
			return mr;
		}
	}

	/**
	 * Create control robot
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping("createCustomRobot")
	public MessageResult createCustomRobot(@RequestBody RobotParams params) {
		logger.info("Create robot: " + params.getCoinName());
		if (exchangeRobotFactory.containsExchangeRobot(params.getCoinName())) {
			MessageResult mr = new MessageResult(500, "Creation failed, robot already exists");
			return mr;
		} else {
			ExchangeRobotCustom robot = new ExchangeRobotCustom();

			robot.setRobotParamSevice(robotParamService);
			robot.setCustomRobotKlineService(customRobotKlineService); // Note: this is different

			robot.setRobotMarketFeign(robotMarketFeign);

			robot.setRobotParams(params);
			robot.setPlateSymbol(params.getCoinName());
			robot.reloadCustomRobotKline(); // Load today's K-line data

			exchangeRobotFactory.addExchangeRobot(params.getCoinName(), robot);

			new Thread((ExchangeRobotCustom) robot).start();

			MessageResult mr = new MessageResult(0, "Robot created successfully");
			return mr;
		}
	}

}
