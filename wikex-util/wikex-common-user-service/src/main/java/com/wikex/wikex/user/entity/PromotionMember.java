package com.wikex.wikex.user.entity;

import com.wikex.wikex.constant.PromotionLevel;
import com.wikex.wikex.constant.RealNameStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PromotionMember {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String username;
    private String uid;
    private PromotionLevel level;
    private RealNameStatus realNameStatus = RealNameStatus.NOT_CERTIFIED;
}
