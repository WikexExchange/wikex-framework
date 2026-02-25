package com.wikex.wikex.user.vo;

import com.wikex.wikex.constant.LoginTypeEnum;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.Member;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class LoginInfo {
    private String username;
    private Location location;
    private Integer memberLevel;
    private String token;
    private String realName;
    private Country country;
    private String avatar;
    private String promotionCode;
    private long id;
    private int loginCount;
    private String superPartner;
    private String uid;

    private String promotionPrefix;

    private Boolean signInAbility;

    private int firstLevel = 0; // Number of first-level invited friends
    private int secondLevel = 0; // Number of second-level invited friends
    private int thirdLevel = 0; // Number of third-level invited friends

    private Boolean signInActivity;
    private String memberRate;

    private Boolean isGoogleSub;
    private Boolean isAppleSub;

    private int hasPassword = 0;
    private String googleKey;
    private String email;
    private String mobilePhone;
    private Date registrationTime;
    private Date lastLogin;
    private Integer googleState;
    private String codeInviterApplied;
    private LoginTypeEnum loginType;
    private String SecondAuthToken;
    private Boolean IsShowSecondAuth;
    private String RefreshToken;

    public static LoginInfo getLoginInfo(Member member, Country country, String token, Boolean signInActivity,
            String prefix, Date lastLoginBefore) {
        Location location = new Location();
        location.setCity(member.getCity());
        location.setCountry(member.getCountry());
        location.setProvince(member.getProvince());
        location.setDistrict(member.getDistrict());
        return LoginInfo.builder()
                .location(location)
                .memberLevel(member.getMemberLevel())
                .username(member.getUsername())
                .token(token)
                .realName(member.getRealName())
                .country(country)
                .avatar(member.getAvatar())
                .promotionCode(member.getPromotionCode())
                .id(member.getId())
                .loginCount(member.getLoginCount())
                .superPartner(member.getSuperPartner())
                .promotionPrefix(prefix)
                .signInAbility(member.getSignInAbility())
                .signInActivity(signInActivity)
                .firstLevel(member.getFirstLevel())
                .secondLevel(member.getSecondLevel())
                .thirdLevel(member.getThirdLevel())
                .uid(member.getUid())
                .isGoogleSub(member.getGoogleSub() != null && !member.getGoogleSub().isEmpty())
                .isAppleSub(member.getAppleSub() != null && !member.getAppleSub().isEmpty())
                .hasPassword(member.getHasPassword())
                .googleKey(member.getGoogleKey())
                .email(member.getEmail())
                .mobilePhone(member.getMobilePhone())
                .registrationTime(member.getRegistrationTime())
                .lastLogin(lastLoginBefore)
                .googleState(member.getGoogleState())
                .loginType(member.getLoginType())
                .build();

    }
}
