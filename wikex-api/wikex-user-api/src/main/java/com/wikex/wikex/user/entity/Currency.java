package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Currency Exchange Rate Table
 * </p>
 *
 * @author markchao
 * @since 2024-08-12
 */
@ApiModel(value = "Currency Exchange Rate Table")
@Data
@EqualsAndHashCode(callSuper = false)
public class Currency {

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Full Name
     */
    @ApiModelProperty(value = "full name")
    private String fullName;

    /**
     * Symbol
     */
    @ApiModelProperty(value = "symbol")
    private String symbol;

    /**
     * Exchange Rate
     */
    @ApiModelProperty(value = "exchange rate")
    private BigDecimal rate;

    /**
     * Image URL
     */
    @ApiModelProperty(value = "image url")
    private String imageUrl;

    /**
     * Sort Order
     */
    @ApiModelProperty(value = "sort order")
    private Integer sort;

    /**
     * Status 0-disabled 1-enabled
     */
    @ApiModelProperty(value = "status 0-disabled 1-enabled")
    private Integer status;

    /**
     * Update Time
     */
    @ApiModelProperty(value = "update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
