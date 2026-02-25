package com.wikex.wikex.p2p.vo;

import com.wikex.wikex.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Builder
@Data
public class MemberAdvertiseInfo {
    private List<ScanAdvertise> buy;
    private List<ScanAdvertise> sell;
    private String username;
    private String avatar;
    private BooleanEnum realVerified;
    private BooleanEnum emailVerified;
    private BooleanEnum phoneVerified;
    private int transactions;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
