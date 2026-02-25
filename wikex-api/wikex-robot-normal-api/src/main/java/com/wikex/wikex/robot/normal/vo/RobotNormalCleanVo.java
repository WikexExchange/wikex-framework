package com.wikex.wikex.robot.normal.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RobotNormalCleanVo {

    private int moreThanOrder;

    private List<BigDecimal> askList;

    private List<BigDecimal> bidList;
}
