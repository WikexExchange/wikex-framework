package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.PromotionLevel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * User Promotion Entity
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "User Promotion")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberPromotion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Auto-increment primary key ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Invitee's user ID
     */
    @ApiModelProperty(value = "Invitee ID")
    private Long inviteesId;

    /**
     * Inviter's user ID
     */
    @ApiModelProperty(value = "Inviter ID")
    private Long inviterId;

    /**
     * Promotion level: 0 = first-level, 1 = second-level, 2 = third-level
     */
    @ApiModelProperty(value = "Level (0 = first-level, 1 = second-level, 2 = third-level)")
    private Integer level;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    private Date createTime;

}
