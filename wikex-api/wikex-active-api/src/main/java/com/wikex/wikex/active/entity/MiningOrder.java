package com.wikex.wikex.active.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class MiningOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long activityId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Current daily output of the mining machine
     */
    private BigDecimal currentDaysprofit;

    /**
     * End time  2000-01-01 01:00:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private Long memberId;

    private Integer miningDays;

    /**
     * Daily mining output
     */
    private BigDecimal miningDaysprofit;

    private String miningUnit;

    private Integer miningedDays;

    private String image;

    private Integer miningStatus;

    private Integer period;

    private String title;

    private BigDecimal miningInvite;

    private BigDecimal miningInvitelimit;

    /**
     * Total output of the mining machine
     */
    private BigDecimal totalProfit;

    private Integer type;

}
