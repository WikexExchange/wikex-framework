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

@ApiModel(value = "Token Snapshot")
@Data
@EqualsAndHashCode(callSuper = false)
public class TokenSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    @ApiModelProperty(value = "Token Symbol")
    private String tokenSymbol;

    @ApiModelProperty(value = "Snapshot Date (23:50 of the day)")
    private LocalDate snapshotDate;

    @ApiModelProperty(value = "Quantity at 23:50")
    private BigDecimal snapshotQuantity;

    @ApiModelProperty(value = "Price at 23:50 (in USDT)")
    private BigDecimal snapshotPrice;

    @ApiModelProperty(value = "Value at 23:50 (Quantity × Price)")
    private BigDecimal snapshotValue;
}
