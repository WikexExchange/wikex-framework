package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * Quick exchange
 * </p>
 *
 */
@ApiModel(value = "Quick exchange")
@Data
@EqualsAndHashCode(callSuper = false)
public class QuickExchange implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Source exchange amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "Creation date")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "Target exchange amount")
    private BigDecimal exAmount;

    @ApiModelProperty(value = "Source currency")
    private String fromUnit;

    @ApiModelProperty(value = "Exchanger")
    private Long memberId;

    @ApiModelProperty(value = "Exchange rate")
    private BigDecimal rate;

    @ApiModelProperty(value = "Status (0: Not completed, 1: Completed, 2: User cancelled, 3: Admin withdrawn)")
    private Integer status;

    @ApiModelProperty(value = "Target currency")
    private String toUnit;

}
