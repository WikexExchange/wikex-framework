package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * My Stery Box Entity
 * </p>
 *
 * @author tampv
 * @since 2025-11-21
 */
@ApiModel(value = "Setting Entity")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("settings")
public class Settings implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * name (not null)
     */
    @ApiModelProperty(value = "name (not null)")
    @TableField("`name`")
    private String name;

    /**
     * Value
     */
    @ApiModelProperty(value = "Value")
    @TableField("`value`")
    private String value;

    /**
     * Description
     */
    @ApiModelProperty(value = "Description")
    @TableField("`desc`")
    private String desc;

    /**
     * Is Active
     */
    @ApiModelProperty(value = "Is Active")
    @TableField("`is_active`")
    private Boolean is_active;
}
