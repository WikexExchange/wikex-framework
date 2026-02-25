package com.wikex.wikex.p2p.vo;

import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.constant.AppealStatus;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class AppealVo {

    private Long appealId;
    private String advertiseCreaterUserName;
    private String advertiseCreaterName;
    private String customerUserName;
    private String customerName;

    /**
     * Respondent's real name
     */
    private String associateName;

    private Long associateId;
    private Long customerId;
    private Long memberId;

    /**
     * Respondent's username
     */
    private String associateUsername;

    /**
     * Appeal initiator's username
     */
    private String initiatorUsername;

    /**
     * Appeal initiator's real name
     */
    private String initiatorName;

    private Long initiatorId;

    private BigDecimal fee;
    /**
     * Transaction quantity
     */
    private BigDecimal number;

    private BigDecimal money;

    private String orderSn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date transactionTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealWithTime;
    /**
     * Payment method (only one can be selected: alipay, bank, wechatpay), default is bank
     */
    private String payMode;

    private String coinName;

    private OrderStatus orderStatus;
    /**
     * Whether the initiator won the appeal: 0 no, 1 yes
     */
    private BooleanEnum isSuccess;
    /**
     * Advertisement type 0 buy, 1 sell
     */
    private AdvertiseType advertiseType;

    /**
     * Handling status 0 untreated, 1 treated
     */
    private AppealStatus status;

    private String remark;
}
