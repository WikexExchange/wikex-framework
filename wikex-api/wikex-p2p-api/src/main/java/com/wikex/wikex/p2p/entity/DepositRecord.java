package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import java.io.Serializable;

import com.wikex.wikex.constant.DepositStatusEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Deposit Payment Record
 * </p>
 *
 */
@ApiModel(value = "Deposit Payment Record")
@Data
@EqualsAndHashCode(callSuper = false)
public class DepositRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private BigDecimal amount;

    /**
     * 0: Paid  1: Retrieved
     */
    private DepositStatusEnum status;

    private String coinId;

    private Long memberId;

}
