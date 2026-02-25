package com.wikex.wikex.robot.normal.robot;


import com.wikex.wikex.robot.normal.entity.RobotParams;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeRobotFactory {
	
	private ConcurrentHashMap<String, ExchangeRobot> robotMap; // btcusdt -> robot
	
	public ExchangeRobotFactory() {
		robotMap = new ConcurrentHashMap<String, ExchangeRobot>();
	}
	
	public Map<String, ExchangeRobot> getRobotList(){
		return robotMap;
	}

	
	public void addExchangeRobot(String coinName, ExchangeRobot robot) {
		if(!this.containsExchangeRobot(coinName)) {
			this.robotMap.put(coinName, robot);
		}
	}

	
	public void forceAddExchangeRobot(String coinName, ExchangeRobot robot) {
		this.robotMap.put(coinName, robot);
	}

	public void removeExchangeRobot(String coinName) {
		if(this.containsExchangeRobot(coinName)) {
			this.robotMap.remove(coinName);
		}
	}
	
	public boolean containsExchangeRobot(String coinName) {
		return robotMap != null && robotMap.containsKey(coinName);
	}
	
	public ExchangeRobot getExchangeRobot(String coinName) {
		return robotMap.get(coinName);
	}
	
	
	public RobotParams getRobotParams(String coinName) {
		ExchangeRobot robot = robotMap.get(coinName);
		if(robot != null) {
			return robot.getRobotParams();
		}else {
			return null;
		}
	}
	
	
	public void setRobotParams(String coinName, RobotParams params) {
		ExchangeRobot robot = robotMap.get(coinName);
		if(robot != null) {
			robot.setRobotParams(params);
		}
	}

    public void setRobotStrategy(String coinName, Integer strategy, String flowPair, BigDecimal flowPercent) {
		ExchangeRobot robot = robotMap.get(coinName);
		if(robot != null) {
			robot.setRobotStrategy(strategy, flowPair, flowPercent);
		}
    }
}
