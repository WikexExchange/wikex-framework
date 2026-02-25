package com.wikex.wikex.user.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RewardRecordCommisionVO {
    private Long uid;
    private Date createTime;
    private String type;
    private BigDecimal bonus;
}
