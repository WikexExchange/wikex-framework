package com.wikex.wikex.screen;

import com.wikex.wikex.constant.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MemberTransactionScreen extends PageParam{

    
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;
    
    private Integer type;
    private Long inviterId;

    
    private BigDecimal minMoney ;
    private BigDecimal maxMoney ;

    private String symbol;

    
    private BigDecimal minFee ;
    private BigDecimal maxFee ;

    private Long memberId ;

    private String account;

}
