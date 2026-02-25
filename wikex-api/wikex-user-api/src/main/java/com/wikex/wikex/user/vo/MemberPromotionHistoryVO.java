package com.wikex.wikex.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class MemberPromotionHistoryVO {
    private String name;
    private Long uid;
    private Boolean isKyc;
    private Integer level;
    private Date createTime;
}
