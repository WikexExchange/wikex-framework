package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.CommonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Coin Information Table
 * </p>
 * 
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Coin Information Table")
@Data
@EqualsAndHashCode(callSuper = false)
public class Coin implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * Coin name
     */
    @ApiModelProperty(value = "coin name")
    @TableId
    private String name;

    /**
     * Whether auto withdrawal is allowed: 0 - No, 1 - Yes
     */
    @ApiModelProperty(value = "whether auto withdrawal is allowed: 0 - No, 1 - Yes")
    private Integer canAutoWithdraw = BooleanEnum.IS_FALSE.getCode();

    /**
     * Whether recharge is allowed: 0 - No, 1 - Yes
     */
    @ApiModelProperty(value = "whether recharge is allowed: 0 - No, 1 - Yes")
    private Integer canRecharge = BooleanEnum.IS_FALSE.getCode();

    /**
     * Whether transfer is allowed: 0 - No, 1 - Yes
     */
    @ApiModelProperty(value = "whether transfer is allowed: 0 - No, 1 - Yes")
    private Integer canTransfer = BooleanEnum.IS_FALSE.getCode();

    /**
     * Whether withdrawal is allowed: 0 - No, 1 - Yes
     */
    @ApiModelProperty(value = "whether withdrawal is allowed: 0 - No, 1 - Yes")
    private Integer canWithdraw = BooleanEnum.IS_FALSE.getCode();

    /**
     * CNY exchange rate
     */
    @ApiModelProperty(value = "CNY exchange rate")
    private BigDecimal cnyRate;

    /**
     * Whether it is fiat currency: 0 - No, 1 - Yes
     */
    @ApiModelProperty(value = "whether it is fiat currency: 0 - No, 1 - Yes")
    private Boolean hasLegal;

    /**
     * Whether it is a platform coin
     */
    @ApiModelProperty(value = "whether it is a platform coin")
    private Integer isPlatformCoin;

    /**
     * Maximum withdrawal fee
     */
    @ApiModelProperty(value = "maximum withdrawal fee")
    private BigDecimal maxTxFee;

    /**
     * Maximum withdrawal amount
     */
    @ApiModelProperty(value = "maximum withdrawal amount")
    private BigDecimal maxWithdrawAmount;

    /**
     * Minimum recharge amount
     */
    @ApiModelProperty(value = "minimum recharge amount")
    private BigDecimal minRechargeAmount;

    /**
     * Minimum withdrawal fee
     */
    @ApiModelProperty(value = "minimum withdrawal fee")
    private BigDecimal minTxFee;

    /**
     * Minimum withdrawal amount
     */
    @ApiModelProperty(value = "minimum withdrawal amount")
    private BigDecimal minWithdrawAmount;

    /**
     * Miner fee
     */
    @ApiModelProperty(value = "miner fee")
    private BigDecimal minerFee;

    /**
     * Chinese name
     */
    @ApiModelProperty(value = "Chinese name")
    private String nameCn;

    /**
     * Sort order
     */
    @ApiModelProperty(value = "sort order")
    private Integer sort;

    /**
     * Status: 0 - normal, 1 - illegal
     */
    @ApiModelProperty(value = "status: 0 - normal, 1 - illegal")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * Unit
     */
    @ApiModelProperty(value = "unit")
    private String unit;

    /**
     * USD exchange rate
     */
    @ApiModelProperty(value = "USD exchange rate")
    private BigDecimal usdRate;

    /**
     * Withdrawal precision
     */
    @ApiModelProperty(value = "withdrawal precision")
    private Integer withdrawScale;

    /**
     * Automatic withdrawal threshold
     */
    @ApiModelProperty(value = "automatic withdrawal threshold")
    private BigDecimal withdrawThreshold;

    @ApiModelProperty(value = "coin description")
    private String information;

    @ApiModelProperty(value = "coin information link")
    private String infolink;

    @ApiModelProperty(value = "coin icon URL")
    private String iconUrl;

    @ApiModelProperty(value = "total coin amount")
    @TableField(exist = false)
    private BigDecimal allBalance;

    @ApiModelProperty(value = "whether coin is approved")
    private BooleanEnum isApproved = BooleanEnum.IS_FALSE;

}
