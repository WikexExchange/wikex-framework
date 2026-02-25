package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "campaign_settings")
public class CampaignSetting {
    @Indexed
    private String _id;
    private String name;
    private Integer fromTime;
    private Integer toTime;
    private Long lastSyncTime;
    private String lastSyncId;
    private String extraData;
    private boolean isActive;
}
