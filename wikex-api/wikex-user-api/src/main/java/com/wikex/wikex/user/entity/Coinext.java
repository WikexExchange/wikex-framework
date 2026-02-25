package com.wikex.wikex.user.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Coin Extension
 * </p>
 *
 * @author markchao
 * @since 2022-03-20
 */
@ApiModel(value = "Coin Extension")
@Data
@EqualsAndHashCode(callSuper = false)
public class Coinext implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "coin id")
    private Integer coinId;

    /**
     * Coin name
     */
    @ApiModelProperty(value = "coin name")
    private String coinName;

    @ApiModelProperty(value = "protocol")
    private Integer protocol;

    /**
     * Protocol name
     */
    @ApiModelProperty(value = "protocol name")
    private String protocolName;

    /**
     * Withdrawal private key (needs frontend encryption)
     */
    @ApiModelProperty(value = "withdrawal private key")
    private String mainAddress;

    /**
     * Contract address
     */
    @ApiModelProperty(value = "contract address")
    private String ext;

    /**
     * Coin decimals (precision)
     */
    @ApiModelProperty(value = "coin decimals")
    private Integer decimals;

    /**
     * Status: 0 - disabled, 1 - enabled
     */
    @ApiModelProperty(value = "status: 0 - disabled, 1 - enabled")
    private Integer status;

    /**
     * Withdrawal fee (1 = 100%)
     */
    @ApiModelProperty(value = "withdrawal fee (1 = 100%)")
    private BigDecimal withdrawFee;

    /**
     * Minimum withdrawal fee amount
     */
    @ApiModelProperty(value = "minimum withdrawal fee amount")
    private BigDecimal minWithdrawFee;

    /**
     * Whether withdrawal is enabled
     */
    @ApiModelProperty(value = "whether withdrawal is enabled")
    private BooleanEnum isWithdraw = BooleanEnum.IS_FALSE;

    /**
     * Whether recharge is enabled
     */
    @ApiModelProperty(value = "whether recharge is enabled")
    private BooleanEnum isRecharge = BooleanEnum.IS_FALSE;

    /**
     * Whether auto withdrawal is enabled (not recommended)
     */
    @ApiModelProperty(value = "whether auto withdrawal is enabled")
    private BooleanEnum isAutoWithdraw = BooleanEnum.IS_FALSE;

    /**
     * Minimum withdrawal amount
     */
    @ApiModelProperty(value = "minimum withdrawal amount")
    private BigDecimal minWithdraw;

    /**
     * Maximum withdrawal amount
     */
    @ApiModelProperty(value = "maximum withdrawal amount")
    private BigDecimal maxWithdraw;

    /**
     * Minimum recharge amount
     */
    @ApiModelProperty(value = "minimum recharge amount")
    private BigDecimal minRecharge;

    /**
     * Number of confirmations required for deposit to be credited
     */
    @ApiModelProperty(value = "number of confirmations required")
    private Integer confirms;

    /**
     * Address for deposits with memo code filled by user; empty if not needed
     */
    @ApiModelProperty(value = "address for deposits with user memo code")
    private String memoAddress;

}
