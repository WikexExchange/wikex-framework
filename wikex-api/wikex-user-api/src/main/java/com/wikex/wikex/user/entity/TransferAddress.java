package com.wikex.wikex.user.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transfer address
 * </p>
 *
 * @author markchao
 * @since 2021-09-21
 */
@ApiModel(value = "Transfer address")
@Data
@EqualsAndHashCode(callSuper = false)
public class TransferAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Address")
    private String address;

    /**
     * Minimum transfer amount
     */
    @ApiModelProperty(value = "Minimum transfer amount")
    private BigDecimal minAmount;

    @ApiModelProperty(value = "Status")
    private Integer status;

    /**
     * Transfer fee rate
     */
    @ApiModelProperty(value = "Transfer fee rate")
    private BigDecimal transferFee;

    @ApiModelProperty(value = "Coin id")
    private String coinId;

}
