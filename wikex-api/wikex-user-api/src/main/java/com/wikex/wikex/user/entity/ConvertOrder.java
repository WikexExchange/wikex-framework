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
 * Quick Convert Order
 * </p>
 *
 * @author markchao
 * @since 2022-07-12
 */
@ApiModel(value = "Quick Convert Order")
@Data
@EqualsAndHashCode(callSuper = false)
public class ConvertOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Amount after conversion
     */
    @ApiModelProperty(value = "amount after conversion")
    private BigDecimal fromAmount;

    /**
     * Amount before conversion
     */
    @ApiModelProperty(value = "amount before conversion")
    private BigDecimal toAmount;

    @ApiModelProperty(value = "source coin unit")
    private String fromUnit;

    /**
     * User id
     */
    @ApiModelProperty(value = "user id")
    private Long memberId;

    /**
     * Price
     */
    @ApiModelProperty(value = "price")
    private BigDecimal price;

    /**
     * Fee
     */
    @ApiModelProperty(value = "fee")
    private BigDecimal fee;

    @ApiModelProperty(value = "status")
    private Integer status;

    @ApiModelProperty(value = "target coin unit")
    private String toUnit;

    @ApiModelProperty(value = "creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
