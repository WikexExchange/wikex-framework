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
 * Transfer record
 * </p>
 *
 * @author markchao
 * @since 2021-09-21
 */
@ApiModel(value = "Transfer record")
@Data
@EqualsAndHashCode(callSuper = false)
public class TransferRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Address")
    private String address;

    @ApiModelProperty(value = "Amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Fee
     */
    @ApiModelProperty(value = "Fee")
    private BigDecimal fee;

    @ApiModelProperty(value = "Member id")
    private Long memberId;

    @ApiModelProperty(value = "Order number")
    private String orderSn;

    @ApiModelProperty(value = "Remark")
    private String remark;

    @ApiModelProperty(value = "Coin id")
    private String coinId;

}
