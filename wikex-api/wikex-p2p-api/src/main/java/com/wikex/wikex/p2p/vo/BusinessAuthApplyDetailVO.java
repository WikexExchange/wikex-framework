package com.wikex.wikex.p2p.vo;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class BusinessAuthApplyDetailVO {

    private Long id;

    /**
     * Certification information
     */
    private JSONObject info;

    /**
     * Certification status
     */
    @Enumerated(value = EnumType.ORDINAL)
    private CertifiedBusinessStatus status;

    /**
     * Certification time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /**
     * Real name
     */
    private String realName;

    /**
     * Reason for certification failure
     */
    private String detail;

    private BigDecimal amount;

    @JsonIgnore
    private String authInfo;
}
