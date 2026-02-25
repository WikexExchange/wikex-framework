package com.wikex.wikex.p2p.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * OTC Coin
 * </p>
 * Represents an Over-The-Counter trading coin.
 * </p>
 */
@ApiModel(value = "OTC Coin")
@Data
@EqualsAndHashCode(callSuper = false)
public class OtcCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * OTC currency ID
     */
    @ApiModelProperty(value = "OTC currency ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Minimum posting amount for buy advertisements
     */
    @ApiModelProperty(value = "Minimum posting amount for buy advertisements")
    private BigDecimal buyMinAmount;

    /**
     * Whether it is a platform coin (0: No, 1: Yes)
     */
    @ApiModelProperty(value = "Whether it is a platform coin (0: No, 1: Yes)")
    private Integer isPlatformCoin;

    /**
     * Transaction fee rate
     */
    @ApiModelProperty(value = "Transaction fee rate")
    private BigDecimal jyRate;

    /**
     * OTC currency name
     */
    @ApiModelProperty(value = "OTC currency name")
    private String name;

    /**
     * OTC currency unit name in Chinese
     */
    @ApiModelProperty(value = "OTC currency unit name in Chinese")
    private String nameCn;

    /**
     * Minimum posting amount for sell advertisements
     */
    @ApiModelProperty(value = "Minimum posting amount for sell advertisements")
    private BigDecimal sellMinAmount;

    /**
     * Display order
     */
    @ApiModelProperty(value = "Display order")
    private Integer sort;

    /**
     * Status (0: Active, 1: Invalid)
     */
    @ApiModelProperty(value = "Status (0: Active, 1: Invalid)")
    private Integer status;

    /**
     * OTC currency unit
     */
    @ApiModelProperty(value = "OTC currency unit")
    private String unit;

    @TableField(exist = false)
    private String marketPrice;

    @TableField(exist = false)
    private BigDecimal sell_min_amount;

    @TableField(exist = false)
    private BigDecimal buy_min_amount;
}
