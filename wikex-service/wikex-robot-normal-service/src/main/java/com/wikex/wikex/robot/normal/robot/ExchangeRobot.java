package com.wikex.wikex.robot.normal.robot;


import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.exchange.feign.MonitorFeign;
import com.wikex.wikex.robot.market.feign.RobotMarketFeign;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.robot.normal.service.CustomRobotKlineService;
import com.wikex.wikex.robot.normal.service.RobotParamService;

import java.math.BigDecimal;
import java.time.Instant;

public interface ExchangeRobot{
//	public static final String EXCHANGE_GATE_WAY = "http://172.28.5.219:8801";
	
	public BigDecimal getOuterPrice();

	public RobotParams getRobotParams();
	
	public void setRobotParams(RobotParams params);
	
	public void setPlateSymbol(String symbol);

	public void updateRobotParams(RobotParams params);
	
	public void startRobot();
	
	public void stopRobot();
	
	public void setRobotParamSevice(RobotParamService service);
	
	public void setRobotMarketFeign(RobotMarketFeign robotMarketFeign);

	public void setExchangeOrderFeign(ExchangeOrderFeign exchangeOrderFeign);

	public void setMonitorFeign(MonitorFeign monitorFeign);

	
	public Instant getLastSendOrderTime();
	
	public void setCustomRobotKlineService(CustomRobotKlineService service); 
	
	public boolean reloadCustomRobotKline();

	public void interrupt();

	public boolean isRunning();

	public void setRobotStrategy(int strategy, String flowPair, BigDecimal flowPercent);
}
