package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Date;

/**
 * <p>
 * My Stery Box Entity
 * </p>
 *
 * @author tampv
 * @since 2025-11-05
 */
@ApiModel(value = "My Stery Box Entity")
@Data
@EqualsAndHashCode(callSuper = false)
public class MySteryBox implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Box name (not null)
     */
    @ApiModelProperty(value = "Box name (not null)")
    @TableField( "`name`")
    private String name;

    /**
     * Code
     */
    @ApiModelProperty(value = "Code")
    @TableField( "`code`")
    private String code;

    /**
     * Type
     */
    @ApiModelProperty(value = "Type (1: USDT, 2: NFT, 3: VOUCHER)")
    @TableField( "`type`")
    private Integer type;

    /**
     * Is Active
     */
    @ApiModelProperty(value = "Is Active")
    private Boolean is_active;

    /**
     * Member ID
     */
    @ApiModelProperty(value = "Member ID")
    private Long member_id;

    /**
     * Member Active At
     */
    @ApiModelProperty(value = "Member Active At")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date member_active_at;

    /**
     * Value
     */
    @ApiModelProperty(value = "Value")
    @TableField( "`value`")
    private String value;

    /**
     * Claim Reward Open At
     */
    @ApiModelProperty(value = "Claim reward open at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date claim_reward_open_at;

    /**
     * Extra Data
     */
    @ApiModelProperty(value = "Extra Data")
    private String extra_data;

}
