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
 * Auto Collection Configuration
 * </p>
 *
 * @author markchao
 * @since 2022-03-20
 */
@ApiModel(value = "Auto Collection Configuration")
@Data
@EqualsAndHashCode(callSuper = false)
public class Automainconfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Coin ID
     */
    @ApiModelProperty(value = "coin ID")
    private Integer coinId;

    /**
     * Coin name
     */
    @ApiModelProperty(value = "coin name")
    private String coinName;

    /**
     * Minimum collection amount
     */
    @ApiModelProperty(value = "minimum collection amount")
    private BigDecimal minNum;

    /**
     * Coin protocol
     */
    @ApiModelProperty(value = "coin protocol")
    private Integer protocol;

    /**
     * Collection address (needs encryption on frontend)
     */
    @ApiModelProperty(value = "collection address")
    private String address;

}
