package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.RewardRecordType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Reward record
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Reward record")
@Data
@EqualsAndHashCode(callSuper = false)
public class RewardRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key id, auto-increment
     */
    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Amount
     */
    @ApiModelProperty(value = "Amount")
    private BigDecimal amount;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Remark
     */
    @ApiModelProperty(value = "Remark")
    private String remark;

    /**
     * Type: 0 - Promotion, 1 - Activity, 2 - Dividend
     */
    @ApiModelProperty(value = "Type: 0 - Promotion, 1 - Activity, 2 - Dividend")
    private RewardRecordType type;

    /**
     * Coin id
     */
    @ApiModelProperty(value = "Coin id")
    private String coinId;

    /**
     * Member id
     */
    @ApiModelProperty(value = "Member id")
    private Long memberId;

    /**
     * Refer Id
     */
    @ApiModelProperty(value = "Refer Id")
    private String referId;

}
