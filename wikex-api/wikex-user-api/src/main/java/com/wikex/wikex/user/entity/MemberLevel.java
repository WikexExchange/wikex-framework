package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * Member Level Entity
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Level Entity")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Is default level
     */
    @ApiModelProperty(value = "Is default level")
    private Boolean isDefault;

    /**
     * Member level name (not null)
     */
    @ApiModelProperty(value = "Member level name (not null)")
    private String name;

    /**
     * Remark or notes
     */
    @ApiModelProperty(value = "Remark or notes")
    private String remark;

}
