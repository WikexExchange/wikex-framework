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
 * Member Invitation Ranking
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member Invitation Ranking")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberInviteStasticRank implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Is robot (0: no, 1: yes)
     */
    @ApiModelProperty(value = "Is robot (0: no, 1: yes)")
    private Integer isRobot;

    /**
     * Number of level 1 invited friends
     */
    @ApiModelProperty(value = "Number of level 1 invited friends")
    private Integer levelOne;

    /**
     * Number of level 2 invited friends
     */
    @ApiModelProperty(value = "Number of level 2 invited friends")
    private Integer levelTwo;

    /**
     * Member ID
     */
    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    /**
     * Statistics date
     */
    @ApiModelProperty(value = "Statistics date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date stasticDate;

    /**
     * Type: 0 = daily ranking, 1 = weekly ranking, 2 = monthly ranking
     */
    @ApiModelProperty(value = "Type: 0 = daily, 1 = weekly, 2 = monthly")
    private Integer type;

    /**
     * User identifier (phone or email)
     */
    @ApiModelProperty(value = "User identifier (phone or email)")
    private String userIdentify;

}
