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
public class LockedOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long activityId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Current period release amount
     */
    private BigDecimal currentReleaseamount;

    /**
     * End time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private String image;

    private Integer lockedDays;

    private BigDecimal lockedInvite;

    private BigDecimal lockedInvitelimit;

    private Integer lockedStatus;

    private Long memberId;

    /**
     * Original period release amount
     */
    private BigDecimal originReleaseamount;

    private Integer period;

    private BigDecimal releaseCurrentpercent;

    private BigDecimal releasePercent;

    private BigDecimal releaseTimes;

    private Integer releaseType;

    private String releaseUnit;

    private Integer releasedDays;

    private String title;

    /**
     * Total locked
     */
    private BigDecimal totalLocked;

    /**
     * Total release
     */
    private BigDecimal totalRelease;


}
