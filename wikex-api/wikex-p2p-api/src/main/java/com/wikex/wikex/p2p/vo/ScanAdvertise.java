package com.wikex.wikex.p2p.vo;

import com.wikex.wikex.constant.AdvertiseType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Builder
@Data
public class ScanAdvertise {
    private Long memberId;
    private String memberName;
    private String avatar;
    private long advertiseId;
    /**
     * Number of transactions
     */
    private int transactions;
    private String localCurrency;
    /**
     * Current price
     */
    private BigDecimal price;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    /**
     * Remaining coin amount
     */
    private BigDecimal remainAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String payMode;
    private long coinId;
    private String unit;
    private String coinName;
    private String coinNameCn;
    /**
     * 0: unverified user, 1: verified user, 2: certified business
     */
    private int level;

    private AdvertiseType advertiseType;

    /**
     * Field for querying advertisement type
     */
    private int advType;
    private BigDecimal premiseRate;

}
