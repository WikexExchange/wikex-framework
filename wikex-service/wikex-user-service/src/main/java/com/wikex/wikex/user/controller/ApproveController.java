package com.wikex.wikex.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.AuditStatus;
import com.wikex.wikex.constant.RealNameStatus;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.service.CountryService;
import com.wikex.wikex.user.service.MemberApplicationService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static com.wikex.wikex.constant.BooleanEnum.IS_FALSE;
import static com.wikex.wikex.constant.BooleanEnum.IS_TRUE;
import static org.springframework.util.Assert.*;

@Api(tags = "User Center Authentication")
@RestController
@RequestMapping("/approve")
@Slf4j
public class ApproveController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(ApproveController.class);

    @Autowired
    private MemberService memberService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private CountryService countryService;

    @Autowired
    private MemberWalletService memberWalletService;

    /**
     * Set or change user avatar
     */
    @ApiOperation(value = "Set or change user avatar")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "url"),
    })
    @PermissionOperation
    @RequestMapping("/change/avatar")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult update(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String url) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        member.setAvatar(url);
        memberService.updateById(member);
        return MessageResult.success();
    }

    /**
     * Security settings
     */
    @ApiOperation(value = "Security settings")
    @PermissionOperation
    @RequestMapping("/security/setting")
    public MessageResult securitySetting(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        String idNumber = member.getIdNumber();
        MemberSecurity memberSecurity = MemberSecurity.builder().username(member.getUsername())
                .createTime(member.getRegistrationTime())
                .id(member.getId())
                .emailVerified(StringUtils.isEmpty(member.getEmail()) ? IS_FALSE.getCode() : IS_TRUE.getCode())
                .email(member.getEmail())
                .mobilePhone(member.getMobilePhone())
                .fundsVerified(StringUtils.isEmpty(member.getJyPassword()) ? IS_FALSE.getCode() : IS_TRUE.getCode())
                .loginVerified(IS_TRUE.getCode())
                .phoneVerified(StringUtils.isEmpty(member.getMobilePhone()) ? IS_FALSE.getCode() : IS_TRUE.getCode())
                .realName(member.getRealName())
                .idCard(StringUtils.isEmpty(idNumber) ? null
                        : idNumber.substring(0, 2) + "**********" + idNumber.substring(idNumber.length() - 2))
                .realVerified(StringUtils.isEmpty(member.getRealName()) ? IS_FALSE.getCode() : IS_TRUE.getCode())
                .realAuditing(member.getRealNameStatus().equals(RealNameStatus.AUDITING.getCode()) ? IS_TRUE.getCode()
                        : IS_FALSE.getCode())
                .avatar(member.getAvatar())
                .googleStatus(member.getGoogleState())
                .build();
        if (memberSecurity.getRealAuditing().equals(IS_FALSE.getCode())
                && memberSecurity.getRealVerified().equals(IS_FALSE.getCode())) {
            List<MemberApplication> memberApplication = memberApplicationService.findLatelyReject(member.getId());
            memberSecurity.setRealNameRejectReason(memberApplication == null || memberApplication.size() == 0 ? null
                    : memberApplication.get(0).getRejectReason());
        }
        MessageResult result = MessageResult.success("success");
        result.setData(memberSecurity);
        return result;
    }

    /**
     * Set transaction password
     */
    @ApiOperation(value = "Set transaction password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jyPassword", value = "Transaction password"),
    })
    @PermissionOperation
    @RequestMapping("/transaction/password")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult approveTransaction(String jyPassword,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(jyPassword, msService.getMessage("MISSING_JY_PASSWORD"));
        isTrue(jyPassword.length() >= 6 && jyPassword.length() <= 20,
                msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.getById(user.getId());
        Assert.isNull(member.getJyPassword(), msService.getMessage("REPEAT_SETTING"));
        // Generate password
        String jyPass = MD5.md5(jyPassword + member.getSalt()).toLowerCase();
        member.setJyPassword(jyPass);
        memberService.updateById(member);
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * Modify transaction password
     */
    @ApiOperation(value = "Modify transaction password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPassword", value = "Old password"),
            @ApiImplicitParam(name = "newPassword", value = "New password"),
    })
    @PermissionOperation
    @RequestMapping("/update/transaction/password")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateTransaction(String oldPassword, String newPassword,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(oldPassword, msService.getMessage("MISSING_OLD_JY_PASSWORD"));
        hasText(newPassword, msService.getMessage("MISSING_NEW_JY_PASSWORD"));
        isTrue(newPassword.length() >= 6 && newPassword.length() <= 20,
                msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.getById(user.getId());
        isTrue(MD5.md5(oldPassword + member.getSalt()).toLowerCase().equals(member.getJyPassword()),
                msService.getMessage("ERROR_JYPASSWORD"));
        member.setJyPassword(MD5.md5(newPassword + member.getSalt()).toLowerCase());
        memberService.updateById(member);
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * Reset transaction password
     */
    @ApiOperation(value = "Reset transaction password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newPassword", value = "New password"),
            @ApiImplicitParam(name = "code", value = "Verification code"),
    })
    @PermissionOperation
    @RequestMapping("/reset/transaction/password")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult resetTransaction(String newPassword, String code,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(newPassword, msService.getMessage("MISSING_NEW_JY_PASSWORD"));
        isTrue(newPassword.length() >= 6 && newPassword.length() <= 20,
                msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(SysConstant.PHONE_RESET_TRANS_CODE_PREFIX + user.getMobilePhone());
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        if (!code.equals(cache.toString())) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.PHONE_RESET_TRANS_CODE_PREFIX + user.getMobilePhone());
        }
        Member member = memberService.getById(user.getId());
        member.setJyPassword(MD5.md5(newPassword + member.getSalt()).toLowerCase());
        memberService.updateById(member);
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * Bind mobile phone
     */
    @ApiOperation(value = "Bind mobile phone")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "Password"),
            @ApiImplicitParam(name = "phone", value = "Mobile phone number"),
            @ApiImplicitParam(name = "code", value = "Verification code"),
    })
    @PermissionOperation
    @RequestMapping("/bind/phone")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindPhone(HttpServletRequest request, String password, String phone, String code,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
        hasText(phone, msService.getMessage("MISSING_PHONE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        if ("Vietnam".equals(user.getLocation().getCountry())) {
            if (!ValidateUtil.isMobilePhone(phone.trim())) {
                return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
            }
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        Member member1 = memberService.findByPhone(phone);
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        if (!code.equals(cache.toString())) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
        }
        Member member = memberService.getById(user.getId());
        isTrue(member.getMobilePhone() == null, msService.getMessage("REPEAT_PHONE_REQUEST"));
        if (member.getPassword().equals(MD5.md5(password + member.getSalt()).toLowerCase())) {
            member.setMobilePhone(phone);
            memberService.updateById(member);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        } else {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        }
    }

    /**
     * Change login password
     */
    @ApiOperation(value = "Change login password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPassword", value = "Old password"),
            @ApiImplicitParam(name = "newPassword", value = "New password"),
            @ApiImplicitParam(name = "code", value = "Verification code"),
    })
    @PermissionOperation
    @RequestMapping("/update/password")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateLoginPassword(HttpServletRequest request, String oldPassword, String newPassword,
            String code, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(oldPassword, msService.getMessage("MISSING_OLD_PASSWORD"));
        hasText(newPassword, msService.getMessage("MISSING_NEW_PASSWORD"));
        isTrue(newPassword.length() >= 6 && newPassword.length() <= 20,
                msService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(SysConstant.PHONE_UPDATE_PASSWORD_PREFIX + user.getMobilePhone());
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        if (!code.equals(cache.toString())) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.PHONE_UPDATE_PASSWORD_PREFIX + user.getMobilePhone());
        }
        Member member = memberService.getById(user.getId());
        request.removeAttribute(SysConstant.SESSION_MEMBER);
        isTrue(MD5.md5(oldPassword + member.getSalt()).toLowerCase().equals(member.getPassword()),
                msService.getMessage("PASSWORD_ERROR"));
        member.setPassword(MD5.md5(newPassword + member.getSalt()).toLowerCase());
        memberService.updateById(member);

        String tokenKey = SysConstant.TOKEN_MEMBER + member.getId() + "_" + IPUtils.getIpAddr(request);
        redisTemplate.delete(tokenKey);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * Bind email
     */
    @ApiOperation(value = "Bind email")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "Password"),
            @ApiImplicitParam(name = "code", value = "Verification code"),
            @ApiImplicitParam(name = "email", value = "Email address"),
    })
    @PermissionOperation
    @RequestMapping("/bind/email")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindEmail(HttpServletRequest request, String password, String code, String email,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        hasText(email, msService.getMessage("MISSING_EMAIL"));
        isTrue(ValidateUtil.isEmail(email), msService.getMessage("EMAIL_FORMAT_ERROR"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(SysConstant.EMAIL_BIND_CODE_PREFIX + email);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        isTrue(code.equals(cache.toString()), msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        Member member = memberService.getById(user.getId());
        isTrue(member.getEmail() == null, msService.getMessage("REPEAT_EMAIL_REQUEST"));
        if (!MD5.md5(password + member.getSalt()).toLowerCase().equals(member.getPassword())) {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        } else {
            member.setEmail(email);
            memberService.updateById(member);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        }
    }

    /**
     * Real name authentication
     */
    @ApiOperation(value = "Real name authentication")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "realName", value = "Real name"),
            @ApiImplicitParam(name = "idCard", value = "ID number"),
            @ApiImplicitParam(name = "idCardFront", value = "ID Card front"),
            @ApiImplicitParam(name = "idCardBack", value = "ID Card back"),
            @ApiImplicitParam(name = "handHeldIdCard", value = "Hand-held ID Card"),
    })
    @PermissionOperation
    @RequestMapping("/real/name")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realApprove(String realName, String idCard, String idCardFront,
            String idCardBack, String handHeldIdCard,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(realName, msService.getMessage("MISSING_REAL_NAME"));
        hasText(idCard, msService.getMessage("MISSING_ID_CARD"));
        hasText(idCardFront, msService.getMessage("MISSING_ID_CARD_FRONT"));
        hasText(idCardBack, msService.getMessage("MISSING_ID_CARD_BACK"));
        hasText(handHeldIdCard, msService.getMessage("MISSING_ID_CARD_HAND"));
        Member member = memberService.getById(user.getId());
        Country country = countryService.findOne(member.getLocal());
        if ("Vietnam".equals(country.getEnName())) {
            isTrue(ValidateUtil.isChineseName(realName), msService.getMessage("REAL_NAME_ILLEGAL"));
            isTrue(IdcardValidator.isValidate18Idcard(idCard), msService.getMessage("ID_CARD_ILLEGAL"));
        }
        isTrue(member.getRealNameStatus() == RealNameStatus.NOT_CERTIFIED.getCode(),
                msService.getMessage("REPEAT_REAL_NAME_REQUEST"));
        int count = memberApplicationService.queryByIdCard(idCard);
        if (count > 0) {
            return MessageResult.error(msService.getMessage("ONLY_AUTHENTICATE_ONCE"));
        }
        MemberApplication memberApplication = new MemberApplication();
        memberApplication.setAuditStatus(AuditStatus.AUDIT_ING);
        memberApplication.setRealName(realName);
        memberApplication.setIdCard(idCard);
        memberApplication.setMemberId(member.getId());
        memberApplication.setIdentityCardImgFront(idCardFront);
        memberApplication.setIdentityCardImgInHand(handHeldIdCard);
        memberApplication.setIdentityCardImgReverse(idCardBack);
        memberApplication.setCreateTime(new Date());
        memberApplicationService.save(memberApplication);
        member.setRealNameStatus(RealNameStatus.AUDITING.getCode());
        memberService.updateById(member);
        return MessageResult.success(msService.getMessage("REAL_APPLY_SUCCESS"));
    }

    /**
     * Query real name authentication details
     */
    @ApiOperation(value = "Query real name authentication details")
    @PermissionOperation
    @PostMapping("/real/detail")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realNameApproveDetail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        QueryWrapper<MemberApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", user.getId());
        queryWrapper.orderByDesc("id");
        List<MemberApplication> list = memberApplicationService.list(queryWrapper);
        MemberApplication memberApplication = new MemberApplication();
        if (list != null && list.size() > 0) {
            memberApplication = list.get(0);
        }
        MessageResult result = MessageResult.success();
        result.setData(memberApplication);
        return result;
    }

    /**
     * Account settings
     */
    @ApiOperation(value = "Account settings")
    @PermissionOperation
    @RequestMapping("/account/setting")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult accountSetting(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        hasText(member.getIdNumber(), msService.getMessage("NO_REAL_NAME"));
        hasText(member.getJyPassword(), msService.getMessage("NO_JY_PASSWORD"));
        MemberAccount memberAccount = MemberAccount.builder()
                .realName(member.getRealName())
                .build();
        MessageResult result = MessageResult.success();
        result.setData(memberAccount);
        return result;
    }

    /**
     * Change phone when the original phone is still usable
     */
    @ApiOperation(value = "Change phone when the original phone is still usable")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "Password"),
            @ApiImplicitParam(name = "phone", value = "New phone number"),
            @ApiImplicitParam(name = "url", value = "Verification code"),
    })
    @PermissionOperation
    @RequestMapping("/change/phone")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult changePhone(HttpServletRequest request, String password, String phone, String code,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
        hasText(phone, msService.getMessage("MISSING_PHONE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        Member member1 = memberService.findByPhone(phone);
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(SysConstant.PHONE_CHANGE_CODE_PREFIX + member.getMobilePhone());
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        Country country = countryService.findOne(member.getLocal());
        if ("86".equals(country.getAreaCode())) {
            if (!ValidateUtil.isMobilePhone(phone.trim())) {
                return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
            }
        }
        if (member.getPassword().equals(MD5.md5(password + member.getSalt()).toLowerCase())) {
            if (!code.equals(cache.toString())) {
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.PHONE_CHANGE_CODE_PREFIX + member.getMobilePhone());
            }
            member.setMobilePhone(phone);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        } else {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        }
    }

}
