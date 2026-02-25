package com.wikex.wikex.active.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class ActivityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Activity ID
     */
    private Long activityId;

    /**
     * Obtained currency amount
     */
    private BigDecimal amount;

    /**
     * Settlement unit (when type=3, this is the frozen asset symbol)
     */
    private String baseSymbol;

    /**
     * Currency unit
     */
    private String coinSymbol;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Valid when type=3, represents the frozen asset amount
     */
    private BigDecimal freezeAmount;

    private Long memberId;

    /**
     * Obtained currency price (invalid when type=3)
     */
    private BigDecimal price;

    /**
     * Status: 0: Temporary, 1: Not transacted, 2: Transacted, 3: Revoked
     */
    private Integer state;

    /**
     * Transaction amount, invalid when type=3
     */
    private BigDecimal turnover;

    /**
     * Activity type (0: Unknown, 1: First launch rush purchase, 2: First launch apportionment, 3: Position sharing, 4: Free subscription)
     */
    private Integer type;

    // For display
    @TableField(exist = false)
    private String activityName;


}