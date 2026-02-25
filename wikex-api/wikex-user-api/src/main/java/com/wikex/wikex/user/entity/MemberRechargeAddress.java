package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Member Recharge Address
 * </p>
 * 
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Recharge Address")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberRechargeAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Member ID
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Address
     */
    @ApiModelProperty(value = "Address")
    private String address;

    /**
     * Chain ID
     */
    @ApiModelProperty(value = "Chain ID")
    private Long chainId;

    /**
     * Creation Time
     */
    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
