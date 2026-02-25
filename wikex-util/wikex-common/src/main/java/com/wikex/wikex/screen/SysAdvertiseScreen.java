package com.wikex.wikex.screen;

import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.SysAdvertiseLocation;
import lombok.Data;

@Data
public class SysAdvertiseScreen {
    private String serialNumber;
    private SysAdvertiseLocation sysAdvertiseLocation;
    private CommonStatus status;
}
