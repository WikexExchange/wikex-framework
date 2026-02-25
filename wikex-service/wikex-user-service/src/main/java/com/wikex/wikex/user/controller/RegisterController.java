package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.LoginTypeEnum;
import com.wikex.wikex.constant.MemberLevelEnum;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.LoginByEmail;
import com.wikex.wikex.user.entity.LoginByPhone;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.ResetPassword;
import com.wikex.wikex.user.event.MemberEvent;
import com.wikex.wikex.user.service.CaptchaService;
import com.wikex.wikex.user.service.CountryService;
import com.wikex.wikex.user.service.EmailService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.transform.AuthMember;
// import com.wikex.wikex.user.util.RegistrationTokenUtil;
import com.wikex.wikex.util.*;
import com.wikex.wikex.util.PasswordHasherUtil.PasswordVerificationResult;
import com.wikex.wikex.user.config.GoogleOAuthConfig;
import com.wikex.wikex.user.dto.CaptchaGeetestDTO;
import com.wikex.wikex.user.dto.SendEmailCaptchaRequestDTO;
import com.wikex.wikex.user.dto.SetInviterDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@Api(tags = "Member Registration")
@Controller
@Slf4j
public class RegisterController extends BaseController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private MemberEvent memberEvent;

    @Autowired
    private CountryService countryService;

    @Autowired
    private GoogleOAuthConfig googleConfig;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    // @Autowired
    // private RegistrationTokenUtil registrationTokenUtil;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    @ApiOperation(value = "Countries Supported for Registration")
    @RequestMapping(value = "/support/country", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult allCountry(@RequestHeader(value = "lang") String headerLanguage) {
        MessageResult result = success();
        List<Country> list = countryService.getAllCountry();
        if ("en_VN".equals(headerLanguage)) {
            list = list.stream()
                    .peek(e -> e.setName(e.getZhName()))
                    .sorted(Comparator.comparing(Country::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        } else {
            list = list.stream()
                    .peek(e -> e.setName(e.getEnName()))
                    .sorted(Comparator.comparing(Country::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
        result.setData(list);
        return result;
    }

    @ApiOperation(value = "Check if Username is Duplicate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "Username"),
    })
    @RequestMapping(value = "/register/check/username")
    @ResponseBody
    public MessageResult checkUsername(String username) {
        MessageResult result = success();
        if (memberService.usernameIsExist(username)) {
            result.setCode(500);
            result.setMessage(localeMessageSourceService.getMessage("ACTIVATION_FAILS_USERNAME"));
        }
        return result;
    }

    @ApiOperation(value = "Activate Email")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "key"),
    })
    @RequestMapping(value = "/register/active")
    @Transactional(rollbackFor = Exception.class)
    public String activate(String key, HttpServletRequest request,
            @RequestHeader(value = "lang", required = false) String lang) throws Exception {
        if (StringUtils.isEmpty(key)) {
            request.setAttribute("result", localeMessageSourceService.getMessage("INVALID_LINK"));
            return "registeredResult";
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object info = valueOperations.get(key);
        LoginByEmail loginByEmail = null;
        if (info instanceof LoginByEmail) {
            loginByEmail = (LoginByEmail) info;
        }
        if (loginByEmail == null) {
            request.setAttribute("result", localeMessageSourceService.getMessage("INVALID_LINK"));
            return "registeredResult";
        }
        String normalizedEmail = EmailUtil.normalize(loginByEmail.getEmail());
        if (memberService.emailIsExist(normalizedEmail)) {
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_FAILS_EMAIL"));
            return "registeredResult";
        } else if (memberService.usernameIsExist(loginByEmail.getUsername())) {
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_FAILS_USERNAME"));
            return "registeredResult";
        }

        Long parentId = null;
        // Validate promotion code
        if (StringUtils.hasText(loginByEmail.getPromotion())) {
            Member promotionMember = memberService.findMemberByPromotionCode(loginByEmail.getPromotion().trim());
            if (promotionMember == null) {
                request.setAttribute("result", localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
                return "registeredResult";
            }
            parentId = promotionMember.getId();
        }

        valueOperations.getOperations().delete(key);
        valueOperations.getOperations().delete(loginByEmail.getEmail());

        String loginNo = String.valueOf(idWorkByTwitter.nextId());

        String credentialsSalt = MD5.md5(loginNo);

        String password = MD5.md5(loginByEmail.getPassword() + credentialsSalt).toLowerCase();
        Member member = new Member();

        member.setMemberLevel(MemberLevelEnum.GENERAL.getCode());
        member.setCountry(loginByEmail.getCountry());
        member.setLocal(loginByEmail.getCountry());
        member.setUsername(loginByEmail.getUsername());
        member.setPassword(password);
        member.setEmail(normalizedEmail);
        member.setSalt(credentialsSalt);
        member.setAvatar(
                "https://wikex-exchange.sgp1.digitaloceanspaces.com/e679538f-fa4d-4f7d-899d-8d4dd48774ed.png");
        if (parentId != null && parentId > 0)
            member.setInviterId(parentId);
        memberService.save(member);
        if (member.getId() != null) {
            String code = BitShiftUniqueCodeGenerator.generateUniqueCode();
            member.setPromotionCode(code);
            member.setUid(code.substring(code.length() - 9, code.length()));
            memberService.updateById(member);
            memberEvent.onRegisterSuccess(member, loginByEmail.getPromotion().trim(), lang);
        }
        return "registeredResult";
    }

    @ApiOperation(value = "Register by Email")
    @RequestMapping("/register/email")
    @ResponseBody
    public MessageResult registerByEmail(@Valid LoginByEmail loginByEmail, BindingResult bindingResult,
            @RequestHeader(value = "lang", required = false) String lang)
            throws Exception {
        if (!StringUtils.hasText(lang)) {
            lang = "en_US";
        }
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        String email = EmailUtil.normalize(loginByEmail.getEmail());
        Member existMember = memberService.findByEmail(email);
        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        String credentialsSalt = MD5.md5(loginNo);
        String password = MD5.md5(loginByEmail.getPassword() + credentialsSalt).toLowerCase();
        if (existMember != null) {
            LoginTypeEnum loginType = existMember.getLoginType();
            if (loginType == LoginTypeEnum.GOOGLE) {
                return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_REGISTERED_WITH_GOOGLE"));
            }
            if (loginType == LoginTypeEnum.APPLE) {
                return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_REGISTERED_WITH_APPLE"));
            }
        }
        isTrue(!memberService.usernameIsExist(loginByEmail.getUsername()),
                localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));

        if (!StringUtils.hasText(loginByEmail.getCode())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        if (!emailService.checkCode4Reg(email, loginByEmail.getCode())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

        Long parentId = null;
        // Validate promotion code
        if (StringUtils.hasText(loginByEmail.getPromotion())) {
            Member promotionMember = memberService.findMemberByPromotionCode(loginByEmail.getPromotion().trim());
            if (promotionMember == null) {
                return error(localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
            }
            parentId = promotionMember.getId();
        }

        Member member = new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL.getCode());
        member.setCountry(loginByEmail.getCountry());
        member.setLocal(loginByEmail.getCountry());
        member.setUsername(loginByEmail.getUsername());
        member.setPassword(password);
        member.setEmail(email);
        member.setSalt(credentialsSalt);
        member.setLoginType(LoginTypeEnum.EMAIL);
        member.setHasPassword(1);
        member.setAvatar(
                "https://wikex-exchange.sgp1.digitaloceanspaces.com/e679538f-fa4d-4f7d-899d-8d4dd48774ed.png");
        if (parentId != null && parentId > 0)
            member.setInviterId(parentId);
        String code = BitShiftUniqueCodeGenerator.generateUniqueCode();
        member.setPromotionCode(code);
        member.setUid(code.substring(code.length() - 9, code.length()));
        memberService.save(member);
        memberEvent.onRegisterSuccess(member,
                loginByEmail.getPromotion() != null ? loginByEmail.getPromotion().trim() : null, lang);

        redisTemplate.opsForValue().getOperations()
                .delete(SysConstant.EMAIL_REG_CODE_PREFIX + loginByEmail.getEmail());
        return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));

    }

    @ApiOperation(value = "Register by Phone")
    @RequestMapping("/register/phone")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginByPhone(
            @Valid LoginByPhone loginByPhone,
            BindingResult bindingResult, HttpServletRequest request,
            @RequestHeader(value = "lang", required = false) String lang) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        if ("Vietnam".equals(loginByPhone.getCountry())) {
            Assert.isTrue(ValidateUtil.isMobilePhone(loginByPhone.getPhone().trim()),
                    localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
        }
        String ip = request.getHeader("X-Real-IP");
        String phone = loginByPhone.getPhone();
        ValueOperations valueOperations = redisTemplate.opsForValue();

        isTrue(!memberService.phoneIsExist(phone), localeMessageSourceService.getMessage("PHONE_ALREADY_EXISTS"));
        isTrue(!memberService.usernameIsExist(loginByPhone.getUsername()),
                localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));
        if (StringUtils.hasText(loginByPhone.getPromotion().trim())) {
            isTrue(memberService.userPromotionCodeIsExist(loginByPhone.getPromotion()),
                    localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
        }

        Long parentId = null;
        // Validate promotion code
        if (StringUtils.hasText(loginByPhone.getPromotion())) {
            Member promotionMember = memberService.findMemberByPromotionCode(loginByPhone.getPromotion().trim());
            if (promotionMember == null) {
                return error(localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
            }
            parentId = promotionMember.getId();
        }

        String loginNo = String.valueOf(idWorkByTwitter.nextId());

        String credentialsSalt = MD5.md5(loginNo);

        String password = MD5.md5(loginByPhone.getPassword() + credentialsSalt).toLowerCase();
        Member member = new Member();

        if (!StringUtils.isEmpty(loginByPhone.getSuperPartner())) {
            member.setSuperPartner(loginByPhone.getSuperPartner());
            if (!"0".equals(loginByPhone.getSuperPartner())) {

                member.setStatus(CommonStatus.ILLEGAL.getCode());
            }
        }
        member.setMemberLevel(MemberLevelEnum.GENERAL.getCode());
        member.setLocal(loginByPhone.getCountry());
        member.setCountry(loginByPhone.getCountry());
        member.setUsername(loginByPhone.getUsername());
        member.setPassword(password);
        member.setMobilePhone(phone);
        member.setSalt(credentialsSalt);
        member.setAvatar(
                "https://wikex-exchange.sgp1.digitaloceanspaces.com/e679538f-fa4d-4f7d-899d-8d4dd48774ed.png");
        if (parentId != null && parentId > 0)
            member.setInviterId(parentId);
        String code = BitShiftUniqueCodeGenerator.generateUniqueCode();
        member.setPromotionCode(code);
        member.setUid(code.substring(code.length() - 9, code.length()));
        memberService.save(member);
        memberEvent.onRegisterSuccess(member, loginByPhone.getPromotion().trim(), lang);
        return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));

    }

    @ApiOperation(value = "Register by Phone (Mobile)")
    @RequestMapping("/register/for_phone")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginByPhone4Mobile(
            @Valid LoginByPhone loginByPhone,
            BindingResult bindingResult, HttpServletRequest request,
            @RequestHeader(value = "lang", required = false) String lang) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        if (loginByPhone.getCountry().equals("Vietnam")) {
            Assert.isTrue(ValidateUtil.isMobilePhone(loginByPhone.getPhone().trim()),
                    localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
        }
        String ip = request.getHeader("X-Real-IP");
        String phone = loginByPhone.getPhone();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object code = valueOperations.get(SysConstant.PHONE_REG_CODE_PREFIX + phone);
        isTrue(!memberService.phoneIsExist(phone), localeMessageSourceService.getMessage("PHONE_ALREADY_EXISTS"));
        isTrue(!memberService.usernameIsExist(loginByPhone.getUsername()),
                localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));
        if (StringUtils.hasText(loginByPhone.getPromotion().trim())) {
            isTrue(memberService.userPromotionCodeIsExist(loginByPhone.getPromotion()),
                    localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
        }

        notNull(code, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        if (!code.toString().equals(loginByPhone.getCode())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.PHONE_REG_CODE_PREFIX + phone);
        }

        Long parentId = null;
        // Validate promotion code
        if (StringUtils.hasText(loginByPhone.getPromotion())) {
            Member promotionMember = memberService.findMemberByPromotionCode(loginByPhone.getPromotion().trim());
            if (promotionMember == null) {
                return error(localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
            }
            parentId = promotionMember.getId();
        }

        String loginNo = String.valueOf(idWorkByTwitter.nextId());

        String credentialsSalt = MD5.md5(loginNo);

        String password = MD5.md5(loginByPhone.getPassword() + credentialsSalt).toLowerCase();
        Member member = new Member();

        if (!StringUtils.isEmpty(loginByPhone.getSuperPartner())) {
            member.setSuperPartner(loginByPhone.getSuperPartner());
            if (!"0".equals(loginByPhone.getSuperPartner())) {

                member.setStatus(CommonStatus.ILLEGAL.getCode());
            }
        }
        member.setMemberLevel(MemberLevelEnum.GENERAL.getCode());
        member.setCountry(loginByPhone.getCountry());
        member.setLocal(loginByPhone.getCountry());
        member.setUsername(loginByPhone.getUsername());
        member.setHasPassword(1);
        member.setPassword(password);
        member.setMobilePhone(phone);
        member.setSalt(credentialsSalt);
        if (parentId != null && parentId > 0)
            member.setInviterId(parentId);
        memberService.save(member);
        if (member.getId() != null) {
            String refCode = BitShiftUniqueCodeGenerator.generateUniqueCode();
            member.setPromotionCode(refCode);
            member.setUid(refCode.substring(refCode.length() - 9, refCode.length()));
            memberService.updateById(member);
            memberEvent.onRegisterSuccess(member, loginByPhone.getPromotion().trim(), lang);
            return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
        }
    }

    @ApiOperation(value = "Send Verification Code for Binding Email")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "Email"),
    })
    @RequestMapping("/bind/email/code")
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendBindEmail(String email, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader(value = "lang") String lang) {

        email = EmailUtil.normalize(email);
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        Member member = memberService.getById(user.getId());
        Assert.isNull(member.getEmail(), localeMessageSourceService.getMessage("BIND_EMAIL_REPEAT"));
        Assert.isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
        try {
            emailService.sentBindEmailCode(email, lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @ApiOperation(value = "Send Verification Code for Adding Withdrawal Address")
    @RequestMapping("/add/address/code")
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendAddAddress(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader(value = "lang") String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        String email = member.getEmail();
        if (email == null) {
            return error(localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
        }
        try {
            emailService.sentEmailAddCode(email, lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @ApiOperation(value = "Send Verification Code for Resetting Email")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "Account"),
    })
    @RequestMapping("/reset/email/code")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendResetPasswordCodeWithCaptcha(@RequestBody SendEmailCaptchaRequestDTO input,
            @RequestHeader(value = "lang") String lang) {

        log.info("Received request to send reset password code to email: {}", input);
        if (!captchaService.verifyCaptcha(input.getCaptcha())) {
            return error(localeMessageSourceService.getMessage("GEETEST_FAIL"));
        }
        String email = EmailUtil.normalize(input.getEmail());
        Member member = memberService.findByEmail(email);

        if (member != null) {
            try {
                emailService.sentResetPassword(email, lang);
            } catch (Exception e) {
                e.printStackTrace();
                return error(localeMessageSourceService.getMessage("SEND_FAILED"));
            }
        }
        return success(localeMessageSourceService.getMessage("RESET_PASSWORD_EMAIL_SENT"));
    }

    // @RequestMapping("/reset/email/send-code")
    // @ResponseBody
    // @Transactional(rollbackFor = Exception.class)
    // public MessageResult sendResetPasswordCodeWithCaptcha(@RequestBody
    // SendEmailCaptchaRequestDTO input,
    // @RequestHeader(value = "lang") String lang) {
    // log.info("Received request to send reset password code to email: {}", input);
    // if (!captchaService.verifyCaptcha(input.getCaptcha())) {
    // return error(localeMessageSourceService.getMessage("GEETEST_FAIL"));
    // }
    // Member member = memberService.findByEmail(input.getEmail());
    // notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
    // try {
    // emailService.sentResetPassword(input.getEmail(), lang);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return error(localeMessageSourceService.getMessage("SEND_FAILED"));
    // }
    // return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    // }

    @ApiOperation(value = "Reset Password After Forgotten Password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = "0 for Phone Verification, 1 for Email Verification"),
            @ApiImplicitParam(name = "account", value = "Phone or Email"),
            @ApiImplicitParam(name = "code", value = "Verification Code"),
            @ApiImplicitParam(name = "password", value = "New Password"),
            @ApiImplicitParam(name = "googleCode", value = "Google Authenticator Code (if 2FA enabled)"),
    })
    @RequestMapping(value = "/reset/login/password", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult forgetPassword(@RequestBody ResetPassword input)
            throws Exception {
        Member member = null;
        isTrue(input.getPassword().length() >= 6 && input.getPassword().length() <= 20,
                localeMessageSourceService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        String account = input.getAccount();
        if (input.getMode() == 1) {
            account = EmailUtil.normalize(account);
        }

        if (!StringUtils.hasText(input.getCode())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        if (emailService.checkCode4forgetPassword(account, input.getCode())) {
            if (input.getMode() == 0) {
                member = memberService.findByPhone(input.getAccount());
            } else if (input.getMode() == 1) {
                member = memberService.findByEmail(account);
            }
            notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));

            if (member.getGoogleState() != null && member.getGoogleState() == 1) {
                if (StringUtils.isEmpty(input.getGoogleCode())) {
                    return error(localeMessageSourceService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
                }

                GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
                boolean verified = ga.check_code(member.getGoogleKey(), Long.parseLong(input.getGoogleCode()),
                        System.currentTimeMillis());

                if (!verified) {
                    return error(localeMessageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
                }
            }

            String newPasswordHash = MD5.md5(input.getPassword() + member.getSalt()).toLowerCase();
            isTrue(!newPasswordHash.equals(member.getPassword()),
                    localeMessageSourceService.getMessage("NEW_PASSWORD_SAME_AS_OLD"));
            member.setHasPassword(1);
            member.setPassword(newPasswordHash);
            memberService.saveOrUpdate(member);
            // send email change password success
            // emailService.sentEmailWelcome(password, googleCode);(member.getEmail());

            redisTemplate.opsForValue().getOperations()
                    .delete(SysConstant.RESET_PASSWORD_CODE_PREFIX + account);

            return success(localeMessageSourceService.getMessage("RESET_PASSWORD_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

    }

    @ApiOperation(value = "Send Verification Code for Unbinding Old Email")
    @PermissionOperation
    @RequestMapping(value = "/untie/email/code", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult untieEmailCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader(value = "lang") String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        isTrue(member.getEmail() != null, localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
        try {
            emailService.sentUntieEmailCode(member.getEmail(), lang);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success();
    }

    @ApiOperation(value = "Send Verification Code for New Email")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "Email"),
    })
    @PermissionOperation
    @RequestMapping(value = "/update/email/code", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult updateEmailCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String email,
            @RequestHeader(value = "lang") String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        email = EmailUtil.normalize(email);
        if (memberService.emailIsExist(email)) {
            return MessageResult.error(localeMessageSourceService.getMessage("REPEAT_EMAIL_REQUEST"));
        }
        Member member = memberService.getById(user.getId());
        isTrue(member.getEmail() != null, localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
        try {
            emailService.sentUpdateEmailCode(email, lang);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success();
    }

    @ApiOperation(value = "Send Registration Email Verification Code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "Email"),
            @ApiImplicitParam(name = "code", value = "Verification Code"),
            @ApiImplicitParam(name = "type", value = "Verification Method")
    })
    @RequestMapping("/reg/email/code")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendRegEmail(String email, String type, String code,
            @RequestHeader(value = "lang", required = false) String lang) {
        if (!StringUtils.hasText(lang)) {
            lang = "en_US";
        }
        email = EmailUtil.normalize(email);
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        Assert.isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
        try {
            int gtResult = 0;
            String key = "SLIDER_" + type + "_" + email;
            BoundValueOperations ops = redisTemplate.boundValueOps(key);
            Object per = ops.get();
            if (per != null && !org.apache.commons.lang.StringUtils.isEmpty(code)) {
                String percentage = per.toString();
                if (percentage.equals(code)) {
                    gtResult = 1;
                }
            }
            if (gtResult == 0) {
                return error(localeMessageSourceService.getMessage("GEETEST_FAIL"));
            }
            emailService.sentRegEmailCode(email, lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/reg/email/captcha")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendRegEmail(@RequestBody SendEmailCaptchaRequestDTO emailRequest,
            @RequestHeader(value = "lang") String lang) {
        String email = EmailUtil.normalize(emailRequest.getEmail());
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        try {
            boolean verify = captchaService.verifyCaptcha(emailRequest.getCaptcha());
            if (!verify) {
                return error(localeMessageSourceService.getMessage("GEETEST_FAIL"));
            }
            if (memberService.emailIsExist(email)) {
                Member member = memberService.findByEmail(email);
                return success(localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"), new Object() {
                    public boolean isExist = true;
                    public String email = member.getEmail();
                });
            }
            emailService.sentRegEmailCode(email, lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/check/email/code")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult checkVerificationCode4Email(@RequestParam String email, @RequestParam String code,
            @RequestParam String type) {
        email = EmailUtil.normalize(email);
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        switch (type) {
            case "REGISTER":
                type = SysConstant.EMAIL_REG_CODE_PREFIX;
                break;
            case "RESET_PASSWORD":
                type = SysConstant.RESET_PASSWORD_CODE_PREFIX;
                break;
            case "BIND_EMAIL":
                type = SysConstant.EMAIL_BIND_CODE_PREFIX;
                break;
            case "UPDATE_EMAIL":
                type = SysConstant.EMAIL_UPDATE_CODE_PREFIX;
                break;
            case "BIND_GOOGLE":
                type = SysConstant.BIND_GOOGLE_CODE_PREFIX;
                break;
            case "BIND_APPLE":
                type = SysConstant.BIND_APPLE_CODE_PREFIX;
                break;
            case "ENABLE_2FA":
                type = SysConstant.ENABLE_2FA_CODE_PREFIX;
                break;
            case "DISABLE_2FA":
                type = SysConstant.DISABLE_2FA_CODE_PREFIX;
                break;
            default:
                type = SysConstant.EMAIL_REG_CODE_PREFIX;
                break;
        }

        if (!StringUtils.hasText(code)) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        boolean verify = emailService.checkVerificationCode4Email(email, code, type);
        if (!verify) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }
        return success(localeMessageSourceService.getMessage("VALID_VERIFICATION_CODE"));
    }

    @GetMapping("/ref/check")
    @ResponseBody
    public MessageResult checkRefCode(@RequestParam String refCode) {
        MessageResult result = success();
        Member member = memberService.findMemberByPromotionCode(refCode);
        if (member == null) {
            result.setCode(404);
            result.setMessage(localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
        }
        return result;
    }

    @ApiOperation(value = "Set Refcode/Promotion Code")
    @PermissionOperation
    @PostMapping("set-inviter")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateRefcode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestBody SetInviterDTO body,
            HttpServletRequest request,
            @RequestHeader(value = "lang", required = false) String lang)
            throws Exception {

        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.notNull(user, localeMessageSourceService.getMessage("RE_LOGIN"));

        Member member = memberService.getById(user.getId());
        Assert.notNull(member, localeMessageSourceService.getMessage("USER_NOT_FOUND"));

        // Check inviter for user
        if (member.getInviterId() != null && member.getInviterId() > 0) {
            return error(localeMessageSourceService.getMessage("PROMOTION_CODE_ALREADY_APPLIED"));
        }

        String promotionCode = body.getPromotionCode();
        if (promotionCode == null || promotionCode.isEmpty()) {
            return error(localeMessageSourceService.getMessage("INVALID_PROMOTION_CODE"));
        }

        // Validate promotion code
        Member inviterMember = memberService.findMemberByPromotionCode(promotionCode.trim());
        if (inviterMember == null) {
            return error(localeMessageSourceService.getMessage("INVALID_PROMOTION_CODE"));
        }

        // Check Referral Code not self-used
        if (inviterMember.getId().equals(member.getId())) {
            return error(localeMessageSourceService.getMessage("CANNOT_USE_OWN_PROMOTION_CODE"));
        }

        // Check Referral Code not mutual
        if (inviterMember.getInviterId() != null && inviterMember.getInviterId().equals(member.getId())) {
            return error(localeMessageSourceService.getMessage("CANNOT_CREATE_MUTUAL_REFERRAL"));
        }

        member.setInviterId(inviterMember.getId());
        memberService.updateById(member);
        memberEvent.setMemberInviter(member, inviterMember);

        return success(localeMessageSourceService.getMessage("REFCODE_UPDATED_SUCCESS"));
    }
}
