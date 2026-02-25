package com.wikex.wikex.second.entity;

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
 * Second contract settings
 * </p>
 *
 */
@ApiModel(value = "Second contract settings")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractSecondSet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Indemnity start time (hour:minute:second HH:mm:ss)
     */
    @ApiModelProperty(value = "Indemnity start time (hour:minute:second HH:mm:ss)")
    private String startTime;

    /**
     * Indemnity end time (hour:minute:second HH:mm:ss)
     */
    @ApiModelProperty(value = "Indemnity end time (hour:minute:second HH:mm:ss)")
    private String endTime;

    /**
     * Daily indemnity quantity
     */
    @ApiModelProperty(value = "Daily indemnity quantity")
    private Integer orderNum;

    /**
     * Limit rate (decimal) over which indemnity is not applicable
     */
    @ApiModelProperty(value = "Limit rate (decimal) over which indemnity is not applicable")
    private BigDecimal limitRate;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Update time
     */
    @ApiModelProperty(value = "Update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
