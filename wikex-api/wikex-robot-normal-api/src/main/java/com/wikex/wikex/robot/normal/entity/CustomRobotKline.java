package com.wikex.wikex.robot.normal.entity;

import lombok.Data;

@Data
public class CustomRobotKline {
    private String coinName = ""; // Trading pair name, e.g.: xxxusdt
    private String kdate = ""; // K-line date, e.g.: 2020/02/02
    private String kline = ""; // K-line array JSON string
    private int pricePencent = 0; // Price fluctuation range
}
