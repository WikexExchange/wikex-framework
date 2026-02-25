package com.wikex.wikex.user.vo;

import com.wikex.wikex.constant.PromotionLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class MemberPromotionStasticVO {
    private Long id;

    // Inviter ID
    private Long inviterId;

    // Invitee ID
    private Long inviteesId;

    private PromotionLevel level;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private int count;
}
