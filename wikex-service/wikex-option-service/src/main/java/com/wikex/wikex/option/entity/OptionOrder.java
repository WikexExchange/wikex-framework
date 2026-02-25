package com.wikex.wikex.option.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.constant.ContractOptionOrderResult;
import com.wikex.wikex.constant.ContractOptionOrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class OptionOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    
    private String baseSymbol;

    
    private BigDecimal betAmount;

    
    private String coinSymbol;

    
    private Long createTime;

    
    private ContractOptionOrderDirection direction;

    
    private BigDecimal fee;

    
    private Long memberId;

    
    private Long optionId;

    
    private Integer optionNo;

    
    private ContractOptionOrderResult result;

    
    private BigDecimal rewardAmount;

    
    private ContractOptionOrderStatus status;

    
    private String symbol;

    
    private BigDecimal winFee;
    
}
