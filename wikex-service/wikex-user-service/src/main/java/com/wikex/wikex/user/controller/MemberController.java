package com.wikex.wikex.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.LoginTypeEnum;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.AuthenticationException;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.ChangePassword;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.LinkSocial;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.ResetPassword;
import com.wikex.wikex.user.entity.SetupPassword;
import com.wikex.wikex.user.entity.UpdateMember;
import com.wikex.wikex.user.event.MemberEvent;
import com.wikex.wikex.user.service.CountryService;
import com.wikex.wikex.user.service.EmailService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.LoginInfo;
import com.wikex.wikex.util.GoogleAuthenticatorUtil;
import com.wikex.wikex.util.IPUtils;
import com.wikex.wikex.util.IdWorkByTwitter;
import com.wikex.wikex.util.EmailUtil;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.util.PasswordCheckUtil;
import com.wikex.wikex.util.PasswordHasherUtil;
import com.wikex.wikex.util.PasswordHasherUtil.PasswordVerificationResult;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wikex.wikex.user.config.AppleOAuthConfig;
import com.wikex.wikex.user.config.GoogleOAuthConfig;
import com.wikex.wikex.user.dto.SendEmailCaptchaRequestDTO;
// import com.wikex.wikex.user.util.RegistrationTokenUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * <p>
 * Member User Frontend Controller
 * </p>
 *
 * @author markchao
 * @since 2021-06-14
 */
@Api(tags = "Member User")
@RestController
@RequestMapping("/member")
public class MemberController extends BaseController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CountryService countryService;

    @Autowired
    private LocaleMessageSourceService messageSourceService;

    @Value("${person.promote.prefix:}")
    private String promotePrefix;

    @Autowired
    private GoogleOAuthConfig googleConfig;

    @Autowired
    private AppleOAuthConfig appleConfig;

    /**
     * Get user information
     * 
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get user information")
    @PermissionOperation
    @PostMapping("my-info")
    public MessageResult myInfo(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            HttpServletRequest request) {
        // Validate sign-in activity, currency, member, member wallet
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.notNull(user, messageSourceService.getMessage("RE_LOGIN"));
        Member member = memberService.getById(user.getId());
        Assert.notNull(member, messageSourceService.getMessage("RE_LOGIN"));
        String key = SysConstant.TOKEN_MEMBER + member.getId() + "_" + IPUtils.getIpAddr(request);
        Object rToke = redisTemplate.boundValueOps(key).get();
        QueryWrapper<Country> cq = new QueryWrapper<>();
        cq.eq("zh_name", member.getLocal());
        Country country = countryService.getOne(cq);

        // Get last login
        Date lastLoginBefore = member.getLastLoginTime();
        Member parentMemberIfExist = memberService.getById(member.getInviterId());

        LoginInfo loginInfo = LoginInfo.getLoginInfo(member, country, rToke.toString(), false, promotePrefix,
                lastLoginBefore);
        loginInfo.setCodeInviterApplied(parentMemberIfExist != null ? parentMemberIfExist.getPromotionCode() : null);
        // try {
        // memberEvent.onRegisterSuccess(member,"12321");
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        return success(loginInfo);
    }

    @ApiOperation(value = "Update real name and avatar")
    @PermissionOperation
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateProfile(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestBody UpdateMember updateMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Assert.notNull(user, messageSourceService.getMessage("RE_LOGIN"));

        Member member = memberService.getById(user.getId());
        Assert.notNull(member, messageSourceService.getMessage("USER_NOT_FOUND"));

        if (StringUtils.hasText(updateMember.getRealName())
                && !updateMember.getRealName().equals(member.getRealName())) {
            member.setRealName(updateMember.getRealName());
        }
        if (StringUtils.hasText(updateMember.getAvatar())) {
            member.setAvatar(updateMember.getAvatar());
        }

        boolean updateInfoMember = memberService.updateById(member);
        return updateInfoMember ? success(messageSourceService.getMessage("UPDATE_SUCCESS"))
                : error(messageSourceService.getMessage("UPDATE_FAILED"));
    }

    @ApiOperation(value = "Get promotion rank")
    @PostMapping("promotion-rank")
    public MessageResult getPromotionRank() {

        return null;
    }

    @ApiOperation(value = "Mapping of member ID list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "ids"),
    })
    @PostMapping(value = "mapByMemberIds")
    public Map<Long, Member> mapByMemberIds(@RequestParam("ids") List<Long> ids) {
        if (ids != null && ids.size() > 0) {
            return memberService.mapByMemberIds(ids);
        } else {
            return new HashMap<>();
        }
    }

    @ApiOperation(value = "Link Google account")
    @RequestMapping(value = "/linkGoogle", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @PermissionOperation
    public MessageResult linkGoogle(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @Valid @RequestBody LinkSocial request) throws Exception {
        String idTokenString = request.getIdToken();
        String code = request.getCode();
        String googleCode = request.getGoogleCode();
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        if (member == null) {
            return error(messageSourceService.getMessage("USER_NOT_FOUND"));
        }

        if (!StringUtils.hasText(code)) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        boolean isCodeValid = emailService.checkCode4LinkGoogle(member.getEmail(), code);
        if (!isCodeValid) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

        if (member.getGoogleState() != null && member.getGoogleState() == 1) {
            if (StringUtils.isEmpty(googleCode)) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
            }

            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            boolean verified = ga.check_code(member.getGoogleKey(), Long.parseLong(googleCode),
                    System.currentTimeMillis());

            if (!verified) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
            }
        }
        NetHttpTransport transport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleConfig.getClientId()))
                .setIssuer("https://accounts.google.com")
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            return error(messageSourceService.getMessage("INVALID_GOOGLE_TOKEN"));
        }
        Payload payload = idToken.getPayload();
        String email = EmailUtil.normalize(payload.getEmail());
        String sub = payload.getSubject();
        if (!StringUtils.hasText(email)) {
            return error(messageSourceService.getMessage("GOOGLE_TOKEN_MISSING_EMAIL"));
        }

        Member exist = memberService.findByGoogleSub(sub);
        if (exist != null && !exist.getId().equals(member.getId())) {
            return error(messageSourceService.getMessage("GOOGLE_ACCOUNT_ALREADY_LINKED"));
        }
        member.setGoogleSub(sub);
        if (!StringUtils.hasText(member.getEmail())) {
            member.setEmail(email);
        }
        memberService.updateById(member);
        return success(messageSourceService.getMessage("LINKED_GOOGLE_ACCOUNT_SUCCESS"));
    }

    @ApiOperation(value = "Link Apple account")
    @RequestMapping(value = "/linkApple", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @PermissionOperation
    public MessageResult linkApple(@Valid @RequestBody LinkSocial request,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        String idTokenString = request.getIdToken();
        String code = request.getCode();
        String googleCode = request.getGoogleCode();
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        if (member == null) {
            return error(messageSourceService.getMessage("USER_NOT_FOUND"));
        }

        if (!StringUtils.hasText(code)) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        boolean isCodeValid = emailService.checkCode4LinkApple(member.getEmail(), code);
        if (!isCodeValid) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

        if (member.getGoogleState() != null && member.getGoogleState() == 1) {
            if (StringUtils.isEmpty(googleCode)) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
            }

            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            boolean verified = ga.check_code(member.getGoogleKey(), Long.parseLong(googleCode),
                    System.currentTimeMillis());

            if (!verified) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
            }
        }
        if (!StringUtils.hasText(idTokenString)) {
            return error(messageSourceService.getMessage("MISSING_APPLE_IDENTITY_TOKEN"));
        }
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(idTokenString);
        } catch (ParseException e) {
            return error(messageSourceService.getMessage("INVALID_APPLE_TOKEN_FORMAT"));
        }

        JWKSet publicKeys = JWKSet.load(new URL("https://appleid.apple.com/auth/keys"));
        JWKSelector selector = new JWKSelector(JWKMatcher.forJWSHeader(signedJWT.getHeader()));
        List<JWK> jwks = selector.select(publicKeys);

        if (jwks.isEmpty()) {
            return error(messageSourceService.getMessage("APPLE_PUBLIC_KEY_NOT_FOUND"));
        }

        RSAKey rsaKey = (RSAKey) jwks.get(0);
        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

        boolean isValid = signedJWT.verify(new RSASSAVerifier(publicKey));
        if (!isValid) {
            return error(messageSourceService.getMessage("INVALID_APPLE_TOKEN_SIGNATURE"));
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String sub = claims.getSubject(); // Apple user ID
        String email = EmailUtil.normalize(claims.getStringClaim("email"));

        if (!claims.getAudience().contains(appleConfig.getClientId())) {
            return error(messageSourceService.getMessage("INVALID_APPLE_AUDIENCE"));
        }
        if (!StringUtils.hasText(sub)) {
            return error(messageSourceService.getMessage("APPLE_TOKEN_MISSING_SUBJECT"));
        }

        Member exist = memberService.findByAppleSub(sub);
        if (exist != null && !exist.getId().equals(member.getId())) {
            return error(messageSourceService.getMessage("APPLE_ACCOUNT_ALREADY_LINKED"));
        }
        member.setAppleSub(sub);
        if (!StringUtils.hasText(member.getEmail()) && StringUtils.hasText(email)) {
            member.setEmail(email);
        }
        memberService.updateById(member);
        return success(messageSourceService.getMessage("LINKED_APPLE_ACCOUNT_SUCCESS"));
    }

    @ApiOperation(value = "Unlink Google account")
    @RequestMapping(value = "/unlinkGoogle", method = RequestMethod.GET)
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult unlinkGoogle(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(required = true) String code,
            @RequestParam(required = false) String googleCode) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        if (member == null) {
            return error(messageSourceService.getMessage("USER_NOT_FOUND"));
        }
        if (member.getLoginType() == LoginTypeEnum.GOOGLE && member.getPassword() == null
                && member.getHasPassword() == 0) {
            return error(messageSourceService.getMessage("EMAIL_REQUIRE_PASSWORD_SETUP"));
        }

        if (!StringUtils.hasText(code)) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        boolean isCodeValid = emailService.checkCode4UnlinkGoogle(member.getEmail(), code);
        if (!isCodeValid) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

        if (member.getGoogleState() != null && member.getGoogleState() == 1) {
            if (StringUtils.isEmpty(googleCode)) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
            }

            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            boolean verified = ga.check_code(member.getGoogleKey(), Long.parseLong(googleCode),
                    System.currentTimeMillis());

            if (!verified) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
            }
        }

        memberService.lambdaUpdate()
                .set(Member::getGoogleSub, null)
                .eq(Member::getId, member.getId())
                .update();
        return success(messageSourceService.getMessage("UNLINKED_GOOGLE_ACCOUNT_SUCCESS"));
    }

    @ApiOperation(value = "Unlink Apple account")
    @RequestMapping(value = "/unlinkApple", method = RequestMethod.GET)
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult unlinkApple(@RequestParam(required = true) String code,
            @RequestParam(required = false) String googleCode,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        if (member == null) {
            return error(messageSourceService.getMessage("USER_NOT_FOUND"));
        }

        if (!StringUtils.hasText(code)) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        boolean isCodeValid = emailService.checkCode4UnlinkApple(member.getEmail(), code);
        if (!isCodeValid) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

        if (member.getGoogleState() != null && member.getGoogleState() == 1) {
            if (StringUtils.isEmpty(googleCode)) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
            }

            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            boolean verified = ga.check_code(member.getGoogleKey(), Long.parseLong(googleCode),
                    System.currentTimeMillis());

            if (!verified) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
            }
        }
        memberService.lambdaUpdate()
                .set(Member::getAppleSub, null)
                .eq(Member::getId, member.getId())
                .update();
        return success(messageSourceService.getMessage("UNLINKED_APPLE_ACCOUNT_SUCCESS"));
    }

    @ApiOperation(value = "Change Password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = "0 for Phone Verification, 1 for Email Verification"),
            @ApiImplicitParam(name = "account", value = "Phone or Email", required = true),
            @ApiImplicitParam(name = "code", value = "Verification Code (Email or SMS)", required = true),
            @ApiImplicitParam(name = "oldPassword", value = "Current Password", required = true),
            @ApiImplicitParam(name = "password", value = "New Password", required = true),
            @ApiImplicitParam(name = "googleCode", value = "Google Authenticator Code (if 2FA enabled)")
    })
    @PostMapping("/change-password")
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult changePassword(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestBody ChangePassword input,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, messageSourceService.getMessage("MEMBER_NOT_EXISTS"));

        isTrue(input.getPassword().length() >= 6 && input.getPassword().length() <= 20,
                messageSourceService.getMessage("PASSWORD_LENGTH_ILLEGAL"));

        if (!StringUtils.hasText(input.getCode())) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        boolean isCodeValid = emailService.checkCode4ChangePassword(member.getEmail(), input.getCode());
        if (!isCodeValid) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }
        if (input.getMode() == 0) {
            member = memberService.findByPhone(member.getMobilePhone());
        } else if (input.getMode() == 1) {
            member = memberService.findByEmail(member.getEmail());
        }
        notNull(member, messageSourceService.getMessage("MEMBER_NOT_EXISTS"));

        if (member.getGoogleState() != null && member.getGoogleState() == 1) {
            if (StringUtils.isEmpty(input.getGoogleCode())) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
            }

            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            boolean verified = ga.check_code(member.getGoogleKey(), Long.parseLong(input.getGoogleCode()),
                    System.currentTimeMillis());
            if (!verified) {
                return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
            }
        }

        if (!PasswordCheckUtil.isCorrectPassword(input.getOldPassword(), member.getPassword(), member.getSalt())) {
            throw new AuthenticationException(messageSourceService.getMessage("OLD_PASSWORD_INCORRECT"));
        }
        if (PasswordCheckUtil.isCorrectPassword(input.getPassword(), member.getPassword(), member.getSalt())) {
            return error(messageSourceService.getMessage("NEW_PASSWORD_SAME_AS_OLD"));
        }
        String newPasswordHash = MD5.md5(input.getPassword() + member.getSalt()).toLowerCase();

        member.setPassword(newPasswordHash);
        memberService.saveOrUpdate(member);

        redisTemplate.opsForValue().getOperations().delete(SysConstant.CHANGE_PASSWORD_CODE_PREFIX + member.getEmail());
        String tokenKey = SysConstant.TOKEN_MEMBER + member.getId() + "_" + IPUtils.getIpAddr(request);
        redisTemplate.delete(tokenKey);
        // revoke refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", clearCookie.toString());

        return success(messageSourceService.getMessage("CHANGE_PASSWORD_SUCCESS"));
    }

    @ApiOperation(value = "setup Password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = "0 for Phone Verification, 1 for Email Verification"),
            @ApiImplicitParam(name = "account", value = "Phone or Email"),
            @ApiImplicitParam(name = "code", value = "Verification Code"),
            @ApiImplicitParam(name = "password", value = "New Password"),
            @ApiImplicitParam(name = "googleCode", value = "Google Authenticator Code (if 2FA enabled)"),
    })
    @RequestMapping(value = "/setup-password", method = RequestMethod.POST)
    @ResponseBody
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult setUpPassword(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestBody SetupPassword input)
            throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        notNull(member, messageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        isTrue(input.getPassword().length() >= 6 && input.getPassword().length() <= 20,
                messageSourceService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        String password = MD5.md5(input.getPassword() + member.getSalt()).toLowerCase();

        if (!StringUtils.hasText(input.getCode())) {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_REQUIRE"));
        }
        if (emailService.checkCode4SetupPassword(member.getEmail(), input.getCode())) {
            if (input.getMode() == 0) {
                member = memberService.findByPhone(member.getMobilePhone());
            } else if (input.getMode() == 1) {
                member = memberService.findByEmail(member.getEmail());
            }

            notNull(member, messageSourceService.getMessage("MEMBER_NOT_EXISTS"));

            if (member.getGoogleState() != null && member.getGoogleState() == 1) {
                if (StringUtils.isEmpty(input.getGoogleCode())) {
                    return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_REQUIRED"));
                }

                GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
                boolean verified = ga.check_code(member.getGoogleKey(), Long.parseLong(input.getGoogleCode()),
                        System.currentTimeMillis());

                if (!verified) {
                    return error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
                }
            }

            member.setPassword(password);
            member.setHasPassword(1);
            memberService.saveOrUpdate(member);

            redisTemplate.opsForValue().getOperations()
                    .delete(SysConstant.SETUP_PASSWORD_CODE_PREFIX + member.getEmail());

            return success(messageSourceService.getMessage("CHANGE_PASSWORD_SUCCESS"));
        } else {
            return error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }

    }
}
