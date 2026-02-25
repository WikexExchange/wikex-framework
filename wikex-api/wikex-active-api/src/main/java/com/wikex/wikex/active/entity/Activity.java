package com.wikex.wikex.active.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@ApiModel(value = "Innovation Lab")
@Data
@EqualsAndHashCode(callSuper = false)
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Activity link (actual participation page link)
     */
    @ApiModelProperty(value = "Activity link")
    private String activityLink;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Activity introduction
     */
    @ApiModelProperty(value = "Activity introduction")
    private String detail;

    @ApiModelProperty(value = "Image URL")
    private String imageUrl;

    /**
     * Announcement link (activity details link)
     */
    @ApiModelProperty(value = "Announcement link (activity details link)")
    private String noticeLink;

    /**
     * Progress value (e.g. 98 = 98%), valid when type=4 (subscription activity)
     */
    @ApiModelProperty(value = "Progress value (e.g. 98 = 98%), valid when type=4 (subscription activity)")
    private Integer progress=0;

    /**
     * Other activity configuration information (JSON)
     */
    @ApiModelProperty(value = "Other activity configuration information (JSON)")
    private String settings;

    /**
     * Enable/disable (determines whether to display on frontend: 0 disabled, 1 enabled)
     */
    @ApiModelProperty(value = "Enable/disable (determines whether to display on frontend: 0 disabled, 1 enabled)")
    private Integer status = 0;

    /**
     * Progress: 0: Not started, 1: In progress, 2: Distributing, 3: Ended
     */
    @ApiModelProperty(value = "Progress: 0: Not started, 1: In progress, 2: Distributing, 3: Ended")
    private Integer step=0;

    /**
     * Activity title
     */
    @ApiModelProperty(value = "Activity title")
    private String title;

    /**
     * Activity type (0: Unknown, 1: First launch rush purchase, 2: First launch apportionment, 3: Position sharing, 4: Free subscription, 5: Cloud mining machine, 6: Lock-up release)
     */
    @ApiModelProperty(value = "Activity type (0: Unknown, 1: First launch rush purchase, 2: First launch apportionment, 3: Position sharing, 4: Free subscription, 5: Cloud mining machine, 6: Lock-up release)")
    private Integer type;

    /**
     * Accepted currency (e.g. BTC, ETH, etc.)
     */
    @ApiModelProperty(value = "Accepted currency (e.g. BTC, ETH, etc.)")
    private String acceptUnit;

    /**
     * Quantity precision
     */
    @ApiModelProperty(value = "Quantity precision")
    private Integer amountScale = 0;

    /**
     * Banner activity image (Banner)
     */
    @ApiModelProperty(value = "Banner activity image (Banner)")
    private String bannerImageUrl;

    /**
     * Activity details
     */
    @ApiModelProperty(value = "Activity details")
    private String content;

    /**
     * Activity details (English, optional)
     */
    @ApiModelProperty(value = "Activity details (English, optional)")
    private String contentEn;

    /**
     * Activity introduction (English, optional)
     */
    @ApiModelProperty(value = "Activity introduction (English, optional)")
    private String detailEn;

    /**
     * Activity end time (required for rush purchase and apportionment issuance)
     */
    @ApiModelProperty(value = "Activity end time (required for rush purchase and apportionment issuance)")
    private String endTime;

    /**
     * Limit purchase times (0: no limit)
     */
    @ApiModelProperty(value = "Limit purchase times (0: no limit)")
    private Integer limitTimes = 0;

    /**
     * Single person total purchase upper limit (0: no limit)
     */
    @ApiModelProperty(value = "Single person total purchase upper limit (0: no limit)")
    private BigDecimal maxLimitAmout = BigDecimal.ZERO;

    /**
     * Single person total purchase lower limit (0: no limit)
     */
    @ApiModelProperty(value = "Single person total purchase lower limit (0: no limit)")
    private BigDecimal minLimitAmout = BigDecimal.ZERO;

    /**
     * Issue price
     */
    @ApiModelProperty(value = "Issue price")
    private BigDecimal price  = BigDecimal.ZERO;

    /**
     * Price precision
     */
    @ApiModelProperty(value = "Price precision")
    private Integer priceScale = 0;

    /**
     * List display image (Small)
     */
    @ApiModelProperty(value = "List display image (Small)")
    private String smallImageUrl;

    /**
     * Start time
     */
    @ApiModelProperty(value = "Start time")
    private String startTime;

    /**
     * Activity title (English, optional)
     */
    @ApiModelProperty(value = "Activity title (English, optional)")
    private String titleEn;

    /**
     * Total issuance
     */
    @ApiModelProperty(value = "Total issuance")
    private BigDecimal totalSupply = BigDecimal.ZERO;

    /**
     * Sold amount
     */
    @ApiModelProperty(value = "Sold amount")
    private BigDecimal tradedAmount  = BigDecimal.ZERO;

    /**
     * Sale unit (e.g. BTC, ETH, etc.)
     */
    @ApiModelProperty(value = "Sale unit (e.g. BTC, ETH, etc.)")
    private String unit;

    /**
     * Frozen asset amount
     */
    @ApiModelProperty(value = "Frozen asset amount")
    private BigDecimal freezeAmount  = BigDecimal.ZERO;

    /**
     * Require the number of first-level friends not less than this number (0: no limit, >0: limit)
     */
    @ApiModelProperty(value = "Require the number of first-level friends not less than this number (0: no limit, >0: limit)")
    private Integer leveloneCount = 0;

    /**
     * Cloud mining machine: Mining duration periods (only valid for type=5, e.g. 30 days/weeks/months/years, 60 days, etc.)
     */
    @ApiModelProperty(value = "Cloud mining machine: Mining duration periods (only valid for type=5, e.g. 30 days/weeks/months/years, 60 days, etc.)")
    private Integer miningDays = 0;

    /**
     * Cloud mining machine: Daily/weekly/monthly output amount
     */
    @ApiModelProperty(value = "Cloud mining machine: Daily/weekly/monthly output amount")
    private BigDecimal miningDaysprofit  = BigDecimal.ZERO;

    /**
     * Invite friends (and purchase mining machine/lock-up) capacity increase percentage (0 means no increase)
     */
    @ApiModelProperty(value = "Invite friends (and purchase mining machine/lock-up) capacity increase percentage (0 means no increase)")
    private BigDecimal miningInvite = BigDecimal.ZERO;

    /**
     * Capacity increase upper limit percentage (0 means no upper limit)
     */
    @ApiModelProperty(value = "Capacity increase upper limit percentage (0 means no upper limit)")
    private BigDecimal miningInvitelimit = BigDecimal.ZERO;

    /**
     * Cloud mining machine: Currency mined by the cloud mining machine
     */
    @ApiModelProperty(value = "Cloud mining machine: Currency mined by the cloud mining machine")
    private String miningUnit;

    /**
     * Cloud mining machine/lock-up release: Mining output period (0: day, 1: week, 2: month, 3: year)
     */
    @ApiModelProperty(value = "Cloud mining machine/lock-up release: Mining output period (0: day, 1: week, 2: month, 3: year)")
    private Integer miningPeriod = 0;

    /**
     * Purchase condition (e.g. holding XXX coin greater than ZZ to participate in purchase)
     */
    @ApiModelProperty(value = "Purchase condition (e.g. holding XXX coin greater than ZZ to participate in purchase)")
    private BigDecimal holdLimit = BigDecimal.ZERO;

    /**
     * Holding XXX currency unit
     */
    @ApiModelProperty(value = "Holding XXX currency unit")
    private String holdUnit;

    /**
     * Lock-up release: Number of release periods, note that 'Days' in the variable name does not necessarily mean days, it can also be weeks, months, years, etc.
     */
    @ApiModelProperty(value = "Lock-up release: Number of release periods, note that 'Days' in the variable name does not necessarily mean days, it can also be weeks, months, years, etc.")
    private Integer lockedDays;

    /**
     * Lock-up release: Threshold fee
     */
    @ApiModelProperty(value = "Lock-up release: Threshold fee")
    private BigDecimal lockedFee = BigDecimal.ZERO;

    /**
     * Lock-up release: Lock-up release period (0: day, 1: week, 2: month, 3: year)
     */
    @ApiModelProperty(value = "Lock-up release: Lock-up release period (0: day, 1: week, 2: month, 3: year)")
    private Integer lockedPeriod;

    /**
     * Lock-up release: Lock-up currency
     */
    @ApiModelProperty(value = "Lock-up release: Lock-up currency")
    private String lockedUnit;

    /**
     * Lock-up release: Release amount (equal amount release)
     */
    @ApiModelProperty(value = "Lock-up release: Release amount (equal amount release)")
    private BigDecimal releaseAmount = BigDecimal.ZERO;

    /**
     * Lock-up release: Release proportion (proportional release, e.g. input 0.2 means 20% release per period)
     */
    @ApiModelProperty(value = "Lock-up release: Release proportion (proportional release, e.g. input 0.2 means 20% release per period)")
    private BigDecimal releasePercent = BigDecimal.ZERO;

    /**
     * Lock-up release: Release multiplier
     */
    @ApiModelProperty(value = "Lock-up release: Release multiplier")
    private BigDecimal releaseTimes = BigDecimal.ZERO;

    /**
     * Lock-up release: Release type (0: equal amount release, 1: proportional release)
     */
    @ApiModelProperty(value = "Lock-up release: Release type (0: equal amount release, 1: proportional release)")
    private Integer releaseType = 0;


}