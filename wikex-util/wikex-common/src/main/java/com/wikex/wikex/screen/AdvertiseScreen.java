package com.wikex.wikex.screen;

import com.wikex.wikex.constant.AdvertiseControlStatus;
import com.wikex.wikex.constant.AdvertiseType;
import lombok.Data;

@Data
public class AdvertiseScreen extends PageParam{

    AdvertiseType advertiseType;

    String payModel ;

    private String account;

    private Long memberId;

    
    AdvertiseControlStatus status ;

}
