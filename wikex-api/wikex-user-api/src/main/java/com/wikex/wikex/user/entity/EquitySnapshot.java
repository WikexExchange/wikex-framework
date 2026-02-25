package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@ApiModel(value = "User Equity")
@Data
@EqualsAndHashCode(callSuper = false)
public class EquitySnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "member id")
    private Long memberId;

    @ApiModelProperty(value = "date of the equity snapshot")
    private LocalDate date;

    @ApiModelProperty(value = "total equity")
    private BigDecimal totalEquity;

    @ApiModelProperty(value = "total PnL")
    private BigDecimal totalPnl;

    @ApiModelProperty(value = "realized PnL")
    private BigDecimal realizedPnl;

    @ApiModelProperty(value = "unrealized PnL")
    private BigDecimal unrealizedPnl;
}
