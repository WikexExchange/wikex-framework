package com.wikex.wikex.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * Member application information table
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberApplicationVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * Audit status: 0 = pending, 1 = failed, 2 = successful
     */
    private Integer auditStatus;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private String idCard;

    /**
     * ID card front image
     */
    private String identityCardImgFront;

    /**
     * ID card held-in-hand image
     */
    private String identityCardImgInHand;

    /**
     * ID card back image
     */
    private String identityCardImgReverse;

    /**
     * KYC level 2 video verification URL
     */
    private String videoUrl;

    /**
     * Real name
     */
    private String realName;

    /**
     * Rejection reason
     */
    private String rejectReason;

    /**
     * Authentication type: 0 = ID card, 1 = passport, 2 = driver's license
     */
    private Integer type;

    /**
     * Update time
     */
    private LocalDateTime updateTime;

    /**
     * Member ID
     */
    private Long memberId;

    /**
     * KYC level status
     */
    private Integer kycStatus;

    /**
     * Video verification random string
     */
    private String videoRandom;

    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registrationTime;

}
