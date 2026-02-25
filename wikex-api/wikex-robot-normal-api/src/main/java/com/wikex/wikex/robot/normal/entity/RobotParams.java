package com.wikex.wikex.robot.normal.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Modifications to this class require changes in ExchangeCoinController in the Admin project
 * 
 */
@Data
public class RobotParams {
    private String coinName = ""; // e.g., btcusdt
    private boolean isHalt = true; // Whether it is in paused state
    private double startAmount = 0.001; // Minimum trade volume
    private double randRange0 = 20; // Trade volume random range, 1% probability
    private double randRange1 = 4; // Trade volume random range, 9% probability
    private double randRange2 = 1; // Trade volume random range 0.1 (0.0001 ~ 0.09), 20% probability
    private double randRange3 = 0.1; // Trade volume random range 0.1 (0.0001 ~ 0.09), 20% probability
    private double randRange4 = 0.01; // Trade volume random range 0.1 (0.0001 ~ 0.09), 20% probability
    private double randRange5 = 0.001; // Trade volume random range 0.1 (0.0001 ~ 0.09), 20% probability
    private double randRange6 = 0.0001; // Trade volume random range 0.1 (0.0001 ~ 0.09), 10% probability
    private int scale = 4; // Price precision requirement
    private int amountScale = 6; // Amount precision requirement
    private BigDecimal maxSubPrice = new BigDecimal(20); // Difference between highest buy price and lowest sell price exceeds 20 USD
    private int initOrderCount = 30; // Initial order count (must be greater than 24)
    private BigDecimal priceStepRate = new BigDecimal(0.003); // Price change step (0.01 = 1%)
    private int runTime = 1000; // Market data request interval time (5000 = 5 seconds)
    private int robotType = 0; // Robot type
    private int strategyType = 2; // Control robot strategy (1: follow, 2: custom)
    private String flowPair = "BTC/USDT";
    private BigDecimal flowPercent = BigDecimal.valueOf(1);
    private double maxRandomPrice = 30;
    private BigDecimal absolutePriceStep = null;
}
