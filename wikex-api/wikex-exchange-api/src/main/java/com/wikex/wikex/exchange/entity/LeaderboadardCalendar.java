package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "leaderboard_calendars")
public class LeaderboadardCalendar {
    private Integer weekYearNo;
    private Integer monthNo;
    private Integer yearNo;
    private Integer weekNo;
}