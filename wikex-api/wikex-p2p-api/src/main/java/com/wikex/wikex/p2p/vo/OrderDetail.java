package com.wikex.wikex.p2p.vo;

import com.wikex.wikex.user.entity.PaymentTypeRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Builder
@Data
public class OrderDetail {
    private String orderSn;
    private Integer type;
    private String unit;
    private Integer status;
    private BigDecimal price;
    private BigDecimal money;
    private BigDecimal amount;
    private BigDecimal commission;
    private PayInfo payInfo;
    private List<PaymentTypeRecord> payInfos;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;
    private Integer timeLimit;
    private String otherSide;
    private Long myId;
    private Long hisId;
    private String memberMobile;
    private String currency;
}
