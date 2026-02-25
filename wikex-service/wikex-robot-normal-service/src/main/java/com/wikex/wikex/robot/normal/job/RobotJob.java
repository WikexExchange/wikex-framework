package com.wikex.wikex.robot.normal.job;

import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.exchange.feign.MonitorFeign;
import com.wikex.wikex.robot.market.feign.RobotMarketFeign;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.robot.normal.robot.ExchangeRobot;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotCustom;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotFactory;
import com.wikex.wikex.robot.normal.robot.ExchangeRobotNormal;
import com.wikex.wikex.robot.normal.service.CustomRobotKlineService;
import com.wikex.wikex.robot.normal.service.RobotParamService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
public class RobotJob {
	private final static Logger logger = LoggerFactory.getLogger(RobotJob.class);

	@Autowired
	private ExchangeRobotFactory exchangeRobotFactory;
	@Autowired
	private MonitorFeign monitorFeign;
	@Autowired
	private RobotMarketFeign robotMarketFeign;
	@Autowired
	private ExchangeOrderFeign exchangeOrderFeign;
	@Autowired
	private RobotParamService robotParamService;
	@Autowired
	private CustomRobotKlineService customRobotKlineService;

	private boolean inited = false;

	@XxlJob("synchronizeExchangeCenter")
	public void synchronizeExchangeCenter() {
		Map<String, Integer> exchangeCenterCoins = monitorFeign.engines();
		for (Map.Entry<String, Integer> coin : exchangeCenterCoins.entrySet()) {
			String coinName = coin.getKey();
			if (!exchangeRobotFactory.containsExchangeRobot(coinName)) {
				RobotParams params = robotParamService.findOne(coinName);
				if (params != null) {
					ExchangeRobot robot = null;
					if (params.getRobotType() == 0) {
						logger.info("Create new normal robot: " + coinName);
						robot = new ExchangeRobotNormal();
					} else if (params.getRobotType() == 1) {
						logger.info("Create new control robot: " + coinName);
						robot = new ExchangeRobotCustom();
					}
					robot.setRobotParamSevice(robotParamService);
					robot.setCustomRobotKlineService(customRobotKlineService);
					robot.setRobotMarketFeign(robotMarketFeign);
					robot.setExchangeOrderFeign(exchangeOrderFeign);
					robot.setMonitorFeign(monitorFeign);
					robot.setRobotParams(params);
					robot.setPlateSymbol(coinName);

					exchangeRobotFactory.addExchangeRobot(params.getCoinName(), robot);
					if (params.getRobotType() == 0) {
						new Thread((ExchangeRobotNormal) robot).start();
					} else {
						new Thread((ExchangeRobotCustom) robot).start();
					}
				}
			}
		}
	}

	@XxlJob("checkAllRoot")
	public void checkAllRoot() {
		List<String> runRobotList = new ArrayList<>();
		List<String> haltRobotList = new ArrayList<>();
		List<String> errorRobotList = new ArrayList<>();

		Instant currentT = Instant.now();
		exchangeRobotFactory.getRobotList().forEach((symbol, robot) -> {
			if (robot.getRobotParams().isHalt()) {
				haltRobotList.add("[" + symbol + "]");
			} else {
				Instant timestamp = Instant.ofEpochMilli(robot.getLastSendOrderTime().toEpochMilli());
				ZonedDateTime shanghaiTime = timestamp.atZone(ZoneId.of("Asia/Shanghai"));
				runRobotList.add("[" + symbol + "] - Last submitted: " + shanghaiTime.toLocalDateTime());
				// If no orders are submitted for a long time, trigger an alert
				if (currentT.toEpochMilli() - timestamp.toEpochMilli() > robot.getRobotParams().getRunTime() * 5) {
					errorRobotList.add(symbol);
				}
			}
		});

		runRobotList.forEach(logger::info);

		haltRobotList.forEach(logger::info);

		for (String coinName : errorRobotList) {
			// Handle abnormal robots (first stop the original thread)
			ExchangeRobot deadRobot = exchangeRobotFactory.getExchangeRobot(coinName);
			if (deadRobot != null) {
				if (deadRobot.isRunning()) {
					deadRobot.interrupt();
				}
				// Restart robot
				if (deadRobot.getRobotParams().getRobotType() == 0) {
					new Thread((ExchangeRobotNormal) deadRobot).start();
				} else {
					new Thread((ExchangeRobotCustom) deadRobot).start();
				}
			} else {
				// Create new robot (create a new thread of the same type)
				RobotParams params = robotParamService.findOne(coinName);
				if (params != null) {
					ExchangeRobot robot = null;
					if (params.getRobotType() == 0) {
						robot = new ExchangeRobotNormal();
					} else if (params.getRobotType() == 1) {
						robot = new ExchangeRobotCustom();
					}
					robot.setRobotParamSevice(robotParamService);
					robot.setCustomRobotKlineService(customRobotKlineService);
					robot.setRobotMarketFeign(robotMarketFeign);

					robot.setRobotParams(params);
					robot.setPlateSymbol(coinName);

					exchangeRobotFactory.forceAddExchangeRobot(params.getCoinName(), robot);
					if (params.getRobotType() == 0) {
						new Thread((ExchangeRobotNormal) robot).start();
					} else {
						new Thread((ExchangeRobotCustom) robot).start();
					}
				} else {
					logger.error("Failed to retrieve robot parameters: " + coinName);
				}
			}
		}
	}
}
