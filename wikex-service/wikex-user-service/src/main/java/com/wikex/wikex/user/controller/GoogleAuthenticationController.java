package com.wikex.wikex.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.AuthenticationException;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.service.EmailService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.GoogleAuthenticatorUtil;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.util.PasswordHasherUtil;
import com.wikex.wikex.util.PasswordHasherUtil.PasswordVerificationResult;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/google")
public class GoogleAuthenticationController extends BaseController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LocaleMessageSourceService msService;

    @PermissionOperation
    @RequestMapping(value = "/auth/verify", method = RequestMethod.GET)
    public MessageResult verifyGoogleCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            String googleCode) {
        // Enter the code shown on device. Run it quickly before the code expires!
        if (!StringUtils.hasText(googleCode)) {
            return MessageResult.error(msService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
        }
        long code = Long.parseLong(googleCode);

        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        boolean r = ga.check_code(member.getGoogleKey(), code, t);
        if (!r) {
            return MessageResult.error(msService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
        } else {
            return MessageResult.success(msService.getMessage("VERIFICATION_PASSED"));
        }
    }

    @PermissionOperation
    @RequestMapping(value = "/auth/setup", method = RequestMethod.GET)
    public MessageResult setupGoogleAuth(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestHeader(value = "lang", required = false) String lang) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());

        if (member == null) {
            return MessageResult.error(msService.getMessage("NOT_LOGGED_IN"));
        }

        String secret = GoogleAuthenticatorUtil.generateSecretKey();
        if (StringUtils.isEmpty(lang)) {
            lang = "";
        } else {
            lang = lang.toLowerCase();
        }
        String host = "vi_vn".equals(lang) ? "wikex.vn" : "wikex.io";
        String userStr = member.getUsername();

        String qrBarcodeURL = GoogleAuthenticatorUtil.getQRBarcodeURL(
                userStr,
                host,
                secret);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("link", qrBarcodeURL);
        jsonObject.put("secret", secret);

        MessageResult messageResult = new MessageResult();
        messageResult.setData(jsonObject);
        messageResult.setMessage(msService.getMessage("OBTAIN_SUCCESSFUL"));

        return messageResult;
    }

    @PermissionOperation
    @RequestMapping(value = "/auth/disable", method = RequestMethod.GET)
    public MessageResult disableGoogleAuth(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(required = true) String googleCode,
            @RequestParam(required = true) String code) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        String GoogleKey = member.getGoogleKey();

        if (!StringUtils.hasText(code)) {
            return error(msService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        boolean isCodeValid = emailService.checkCode4Disable2FA(member.getEmail(), code);
        if (!isCodeValid) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

        if (!StringUtils.hasText(googleCode)) {
            return MessageResult.error(msService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
        }
        long codeAuth = Long.parseLong(googleCode);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        boolean r = ga.check_code(GoogleKey, codeAuth, t);

        if (!r) {
            return MessageResult.error(msService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
        } else {
            memberService.lambdaUpdate()
                    .set(Member::getGoogleKey, null)
                    .set(Member::getGoogleState, 0)
                    .set(Member::getGoogleDate, new Date())
                    .eq(Member::getId, member.getId())
                    .update();
            boolean result = true;
            if (result) {
                return MessageResult.success(msService.getMessage("UNBIND_SUCCESSFUL"));
            } else {
                return MessageResult.error(msService.getMessage("UNBIND_FAILED"));
            }
        }
    }

    @PermissionOperation
    @RequestMapping(value = "/auth/enable", method = RequestMethod.POST)
    public MessageResult enableGoogleAuth(String googleCode,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            String secret) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());

        if (!StringUtils.hasText(googleCode)) {
            return MessageResult.error(msService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
        }
        long code = Long.parseLong(googleCode);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        boolean r = ga.check_code(secret, code, t);

        if (!r) {
            return MessageResult.error(msService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
        } else {
            member.setGoogleState(1);
            member.setGoogleKey(secret);
            member.setGoogleDate(new Date());
            boolean result = memberService.saveOrUpdate(member);
            if (result) {
                return MessageResult.success(msService.getMessage("BINDING_SUCCESSFUL"));
            } else {
                return MessageResult.error(msService.getMessage("BINDING_FAILED"));
            }
        }
    }
}
