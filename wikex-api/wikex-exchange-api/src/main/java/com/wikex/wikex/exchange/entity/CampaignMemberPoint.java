package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "campaign_member_points")
public class CampaignMemberPoint {
    private String campaignId;
    private Long memberId;
    private Integer type;
    private Integer yearNo;
    private Integer monthNo;
    private Integer weekNo;
    private Integer points;
}
