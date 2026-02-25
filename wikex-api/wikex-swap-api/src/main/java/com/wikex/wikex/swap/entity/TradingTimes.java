package com.wikex.wikex.swap.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Tradable Time Periods
 * </p>
 *
 * @author markchao
 * @since 2024-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TradingTimes implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Trading session start time, format HH:mm (e.g., 09:00)
     */
    private String startTime;

    /**
     * Trading session end time, format HH:mm (e.g., 13:00)
     */
    private String endTime;

    /**
     * Trading pair ID
     */
    private Long contractCoinId;

    /**
     * Remark information, such as adjustments before holidays
     */
    private String remark;

    @TableField(exist = false)
    private String symbol;

}
