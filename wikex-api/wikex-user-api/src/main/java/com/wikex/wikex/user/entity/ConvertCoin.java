package com.wikex.wikex.user.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Quick Convert Coin
 * </p>
 *
 * @author markchao
 * @since 2022-07-12
 */
@ApiModel(value = "Quick Convert Coin")
@Data
@EqualsAndHashCode(callSuper = false)
public class ConvertCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Maximum convert amount
     */
    @ApiModelProperty(value = "maximum convert amount")
    private BigDecimal maxAmount;

    /**
     * Minimum convert amount
     */
    @ApiModelProperty(value = "minimum convert amount")
    private BigDecimal minAmount;

    /**
     * Coin unit
     */
    @ApiModelProperty(value = "coin unit")
    private String coinUnit;

    /**
     * Fee
     */
    @ApiModelProperty(value = "fee")
    private BigDecimal fee;

    /**
     * Sort order
     */
    @ApiModelProperty(value = "sort order")
    private Integer sort;

    /**
     * Status 0 - unavailable, 1 - available
     */
    @ApiModelProperty(value = "status 0 - unavailable, 1 - available")
    private Integer status;

    @ApiModelProperty(value = "creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
