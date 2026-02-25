package com.wikex.wikex.user.vo;

import lombok.Data;
import java.util.List;

@Data
public class LeadboardCalendarVO {
    private Integer year;
    private List<Integer> months;
    private List<Integer> weeks;
}