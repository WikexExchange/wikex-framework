package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * User invitation relationship with upper level and rebate rate
 * </p>
 *
 */
@ApiModel(value = "User invitation relationship with upper level and rebate rate")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberWeightUpper implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * User id
     */
    @ApiModelProperty(value = "User id")
    private Long memberId;

    /**
     * Agent id
     */
    @ApiModelProperty(value = "Agent id")
    private Long firstMemberId;

    /**
     * Rebate rate (%)
     */
    @ApiModelProperty(value = "Rebate rate (%)")
    private Integer rate;

    /**
     * Upper level user ids, comma separated (e.g., 1,2,3)
     */
    @ApiModelProperty(value = "Upper level user ids, comma separated (e.g., 1,2,3)")
    private String upper;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    private LocalDateTime createTime;

}
