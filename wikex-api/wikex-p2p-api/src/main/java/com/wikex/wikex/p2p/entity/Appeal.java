package com.wikex.wikex.p2p.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.AppealStatus;
import com.wikex.wikex.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OTC Appeal
 * Represents a user-submitted complaint or dispute related to an OTC order.
 * Contains details about the appeal process, status, and resolution.
 * 
 * Author: markchao  
 * Since: 2021-08-21
 */
@ApiModel(value = "OTC Appeal")
@Data
@EqualsAndHashCode(callSuper = false)
public class Appeal implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * ID of the associated order or entity being appealed.
     */
    @ApiModelProperty(value = "Associated entity ID")
    private Long associateId;

    /**
     * Date and time when the appeal was created.
     */
    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Date and time when the appeal was processed.
     */
    @ApiModelProperty(value = "Processing time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealWithTime;

    /**
     * ID of the user who initiated the appeal.
     */
    @ApiModelProperty(value = "Initiator ID")
    private Long initiatorId;

    /**
     * Whether the initiator won the appeal (YES/NO).
     */
    @ApiModelProperty(value = "Whether the initiator won the appeal (0 = No, 1 = Yes)")
    private BooleanEnum isSuccess;

    /**
     * Additional remarks or notes regarding the appeal.
     */
    @ApiModelProperty(value = "Remarks")
    private String remark;

    /**
     * Processing status of the appeal.
     */
    @ApiModelProperty(value = "Appeal status (0 = Pending, 1 = Processed)")
    private AppealStatus status;

    /**
     * ID of the administrator who handled the appeal.
     */
    @ApiModelProperty(value = "Admin ID")
    private Long adminId;

    /**
     * Order ID related to the appeal.
     */
    @ApiModelProperty(value = "Order ID")
    private Long orderId;


}
