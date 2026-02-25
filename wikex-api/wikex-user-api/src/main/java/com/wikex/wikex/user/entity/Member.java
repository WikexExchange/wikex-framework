package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Member User
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Member User")
@Data
@EqualsAndHashCode(callSuper = false)
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Number of successful appeals
     */
    @ApiModelProperty(value = "Number of successful appeals")
    private Integer appealSuccessTimes = 0;

    /**
     * Number of appeals
     */
    @ApiModelProperty(value = "Number of appeals")
    private Integer appealTimes = 0;

    /**
     * Real-name verification approval time
     */
    @ApiModelProperty(value = "Real-name verification approval time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applicationTime;

    /**
     * Avatar
     */
    @ApiModelProperty(value = "Avatar")
    private String avatar;

    /**
     * Certified business application time
     */
    @ApiModelProperty(value = "Certified business application time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date certifiedBusinessApplyTime;

    /**
     * Certified business approval time
     */
    @ApiModelProperty(value = "Certified business approval time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date certifiedBusinessCheckTime;

    /**
     * Certified business status
     */
    @ApiModelProperty(value = "Certified business status")
    private CertifiedBusinessStatus certifiedBusinessStatus = CertifiedBusinessStatus.NOT_CERTIFIED;

    /**
     * Channel ID
     */
    @ApiModelProperty(value = "Channel ID")
    private Integer channelId;

    /**
     * Email
     */
    @ApiModelProperty(value = "Email")
    private String email;

    /**
     * First-level referral count
     */
    @ApiModelProperty(value = "First-level referral count")
    private Integer firstLevel = 0;

    /**
     * Google authentication time
     */
    @ApiModelProperty(value = "Google authentication time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date googleDate;

    /**
     * Google key
     */
    @ApiModelProperty(value = "Google key")
    private String googleKey;

    /**
     * Google authentication status 0-disabled 1-enabled
     */
    @ApiModelProperty(value = "Google authentication status 0-disabled 1-enabled")
    private Integer googleState;

    /**
     * ID number (identity card)
     */
    @ApiModelProperty(value = "ID number")
    private String idNumber;

    /**
     * Inviter ID
     */
    @ApiModelProperty(value = "Inviter ID")
    private Long inviterId;

    /**
     * Is channel
     */
    @ApiModelProperty(value = "Is channel")
    private Integer isChannel;

    /**
     * Transaction password
     */
    @ApiModelProperty(value = "Transaction password")
    private String jyPassword;

    /**
     * Last login time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "Last login time")
    private Date lastLoginTime;

    /**
     * City
     */
    @ApiModelProperty(value = "City")
    private String city;

    /**
     * Login count
     */
    @ApiModelProperty(value = "Login count")
    private Integer loginCount = 0;

    /**
     * Login lock status
     */
    @ApiModelProperty(value = "Login lock status")
    private Integer loginLock;

    /**
     * Member level
     */
    @ApiModelProperty(value = "Member level")
    private Integer memberLevel;

    /**
     * Mobile phone number
     */
    @ApiModelProperty(value = "Mobile phone number")
    private String mobilePhone;

    /**
     * Password
     */
    @ApiModelProperty(value = "Password")
    private String password;

    /**
     * Promotion code
     */
    @ApiModelProperty(value = "Promotion code")
    private String promotionCode;

    @ApiModelProperty(value = "uid")
    private String uid;

    /**
     * Whether allowed to publish ads, 1 means allowed
     */
    @ApiModelProperty(value = "Whether allowed to publish ads, 1 means allowed")
    private Integer publishAdvertise = BooleanEnum.IS_TRUE.getCode();

    /**
     * Real name
     */
    @ApiModelProperty(value = "Real name")
    private String realName;

    /**
     * Real-name status
     */
    @ApiModelProperty(value = "Real-name status")
    private Integer realNameStatus = RealNameStatus.NOT_CERTIFIED.getCode();

    /**
     * Registration time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "Registration time")
    private Date registrationTime = new Date();

    /**
     * Salt value (for password hashing)
     */
    @ApiModelProperty(value = "Salt value")
    private String salt;

    /**
     * Second-level referral count
     */
    @ApiModelProperty(value = "Second-level referral count")
    private Integer secondLevel = 0;

    /**
     * Sign-in ability
     */
    @ApiModelProperty(value = "Sign-in ability")
    private Boolean signInAbility = true;

    /**
     * User status
     */
    @ApiModelProperty(value = "User status")
    private Integer status = CommonStatus.NORMAL.getCode();

    /**
     * Third-level referral count
     */
    @ApiModelProperty(value = "Third-level referral count")
    private Integer thirdLevel = 0;

    /**
     * Transaction status
     */
    @ApiModelProperty(value = "Transaction status")
    private Integer transactionStatus = BooleanEnum.IS_TRUE.getCode();

    /**
     * First transaction time
     */
    @ApiModelProperty(value = "First transaction time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date transactionTime;

    /**
     * Number of transactions
     */
    @ApiModelProperty(value = "Number of transactions")
    private Integer transactions = 0;

    /**
     * Username
     */
    @ApiModelProperty(value = "Username")
    private String username;

    /**
     * Country (locale)
     */
    @ApiModelProperty(value = "Country")
    private String local;

    /**
     * Member grade ID
     */
    @ApiModelProperty(value = "Member grade ID")
    private Long memberGradeId;

    /**
     * KYC status (level)
     */
    @ApiModelProperty(value = "KYC status")
    private Integer kycStatus;

    /**
     * Registration bonus points
     */
    @ApiModelProperty(value = "Registration bonus points")
    private Long generalizeTotal;

    /**
     * Inviter's parent ID
     */
    @ApiModelProperty(value = "Inviter's parent ID")
    private Long inviterParentId;

    /**
     * Country
     */
    @ApiModelProperty(value = "Country")
    private String country;

    /**
     * District (street)
     */
    @ApiModelProperty(value = "District")
    private String district;

    /**
     * Province
     */
    @ApiModelProperty(value = "Province")
    private String province;

    /**
     * Whether old user changed password, 0 not changed, 1 changed
     */
    @ApiModelProperty(value = "Whether old user changed password, 0 not changed, 1 changed")
    private String margin;

    /**
     * Super partner flag, 0 normal, 1 super partner
     */
    @ApiModelProperty(value = "Super partner flag, 0 normal, 1 super partner")
    private String superPartner = "0";

    @ApiModelProperty(value = "Google OAuth2 Sub ID")
    private String googleSub;

    @ApiModelProperty(value = "Apple OAuth2 Sub ID")
    private String appleSub;

    @ApiModelProperty(value = "Has set password (0=no, 1=yes)")
    private Integer hasPassword = 0;

    @ApiModelProperty(value = "Login type (0=email, 1=google, 2=apple)")
    private LoginTypeEnum loginType = LoginTypeEnum.EMAIL;

    @ApiModelProperty(value = "Reward Wallet Address")
    private String rewardWalletAddress;
}
