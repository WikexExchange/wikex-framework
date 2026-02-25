package com.wikex.wikex.screen;

import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.constant.ContractOrderEntrustType;
import com.wikex.wikex.constant.ContractOrderType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ContractOrderEntrustScreen extends PageParam{
    private ContractOrderEntrustStatus status;
    private ContractOrderType type;
    private ContractOrderDirection direction;
    private ContractOrderEntrustType entrustType;
    private Long memberId;
    private Long contractId;
    private BigDecimal volume;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;
    private Integer isFromSpot;
    private Integer isBlast;
    private BigDecimal profitAndLoss;
    private String phone;
    private String email;
}
