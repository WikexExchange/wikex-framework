package com.wikex.wikex.user.vo;

import lombok.Data;

@Data
public class RechargeVO {

    private Integer id;

    private String hash;

    private String md5;

    private Integer memberId;

    private Long addTime;

    private Long coinId;

    private String coinName;

    private double money;

    private Integer block;

    private Integer confirms;

    private Integer nConfirms;

    // 0: Not received, 1: Received, -1: Failed
    private Integer status;

    private String send;

    private String address;

    private Integer protocol;

    private String protocolName;

    private String username;

}
