package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.sms.EmailProvider;
import com.wikex.wikex.user.dto.SendEmailCaptchaRequestDTO;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.service.CaptchaService;
import com.wikex.wikex.user.service.EmailService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.GeneratorUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.wikex.wikex.constant.SysConstant.EMAIL_WITHDRAW_MONEY_CODE_PREFIX;
import static com.wikex.wikex.util.MessageResult.error;
import static com.wikex.wikex.util.MessageResult.success;
import static org.springframework.util.Assert.notNull;

@Slf4j
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberService memberService;

    @Autowired
    private EmailService emailService;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    @Autowired
    private EmailProvider emailProvider;

    @Autowired
    private CaptchaService captchaService;

    @PermissionOperation
    @RequestMapping(value = "/transaction/code", method = RequestMethod.POST)
    public MessageResult sendResetTransactionCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang)
            throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        String email = member.getEmail();
        Assert.hasText(member.getEmail(), localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = SysConstant.EMAIL_TRANSACTION_CODE_PREFIX + email;
        if (valueOperations.get(key) != null) {
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND"));
        }
        try {
            String templateName;
            switch (lang == null ? "" : lang.toLowerCase()) {
                case "en_us":
                    templateName = "bindCodeEmailEn.ftl";
                    break;
                case "vi_vn":
                default:
                    templateName = "bindCodeEmailVi.ftl";
                    break;
            }
            ;
            emailProvider.sendEmail(email, randomCode, null, templateName);
            valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @PermissionOperation
    @RequestMapping(value = "/withdraw/code", method = RequestMethod.POST)
    public MessageResult withdrawCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        String email = member.getEmail();
        Assert.hasText(member.getEmail(), localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = EMAIL_WITHDRAW_MONEY_CODE_PREFIX + email;
        if (valueOperations.get(key) != null) {
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND"));
        }
        try {
            String templateName;
            switch (lang == null ? "" : lang.toLowerCase()) {
                case "en_us":
                    templateName = "bindCodeEmailEn.ftl";
                    break;
                case "vi_vn":
                default:
                    templateName = "bindCodeEmailVi.ftl";
                    break;
            }
            ;
            emailProvider.sendEmail(email, randomCode, null, templateName);
            valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @PermissionOperation
    @RequestMapping(value = "/update/password/code", method = RequestMethod.POST)
    public MessageResult updatePasswordCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang)
            throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        String email = member.getEmail();
        Assert.hasText(member.getEmail(), localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = SysConstant.EMAIL_UP_PWD_CODE_PREFIX + email;
        if (valueOperations.get(key) != null) {
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND"));
        }
        try {
            String templateName;
            switch (lang == null ? "" : lang.toLowerCase()) {
                case "en_us":
                    templateName = "bindCodeEmailEn.ftl";
                    break;
                case "vi_vn":
                default:
                    templateName = "bindCodeEmailVi.ftl";
                    break;
            }
            ;
            emailProvider.sendEmail(email, randomCode, null, templateName);
            valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/google/link/code")
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendBindGoogleEmail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sendBindGoogleEmail(member.getEmail(), lang);
        } catch (Exception e) {

            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/apple/link/code")
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendBindAppleEmail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sendBindAppleEmail(member.getEmail(), lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/google/unlink/code")
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendUnbindGoogleEmail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sendUnbindGoogleEmail(member.getEmail(), lang);
        } catch (Exception e) {

            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/apple/unlink/code")
    @PermissionOperation
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendUnbindAppleEmail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sendUnbindAppleEmail(member.getEmail(), lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/enable-2fa/code")
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendEnable2FA(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sendEnable2FA(member.getEmail(), lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/disable-2fa/code")
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendDisable2FA(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sendDisable2FA(member.getEmail(), lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/change/code")
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendChangePassword(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader(value = "lang") String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sentChangePassword(member.getEmail(), lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping("/setup/code")
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendSetupPassword(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader(value = "lang") String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        try {
            emailService.sentSetupPassword(member.getEmail(), lang);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

}
