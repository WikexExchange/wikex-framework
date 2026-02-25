package com.wikex.wikex.rpc.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class WatcherLog {
    private String coinName;
    private Long lastSyncHeight;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastSyncTime;
}
