package com.wikex.wikex.screen;

import com.wikex.wikex.constant.AdminModule;
import lombok.Data;

@Data
public class AccessLogScreen extends PageParam{

    private String adminName;

    private AdminModule module;
}
