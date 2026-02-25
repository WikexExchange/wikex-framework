package com.wikex.wikex.user.controller;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.generator.ImageCaptchaGenerator;
import cloud.tianai.captcha.generator.common.model.dto.ImageCaptchaInfo;
import cloud.tianai.captcha.generator.impl.MultiImageCaptchaGenerator;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.impl.DefaultImageCaptchaResourceManager;
import cloud.tianai.captcha.validator.ImageCaptchaValidator;
import cloud.tianai.captcha.validator.impl.BasicCaptchaTrackValidator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.LoginTypeEnum;
import com.wikex.wikex.constant.MemberLevelEnum;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.AuthenticationException;
import com.wikex.wikex.exception.GAException;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.config.AppleOAuthConfig;
import com.wikex.wikex.user.config.GoogleOAuthConfig;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.LinkSocial;
import com.wikex.wikex.user.entity.LoginBySocial;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.VerifyLogin2FA;
import com.wikex.wikex.user.event.MemberEvent;
import com.wikex.wikex.user.service.CountryService;
import com.wikex.wikex.user.service.EmailService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.system.GeetestLib;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.util.AppleLoginUtil;
import com.wikex.wikex.user.util.GeneratorUserUtil;
// import com.wikex.wikex.user.util.RegistrationTokenUtil;
import com.wikex.wikex.user.vo.LoginInfo;
import com.wikex.wikex.util.*;
import com.wikex.wikex.util.PasswordHasherUtil.PasswordVerificationResult;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.JWKSet;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "Login")
@RestController
@Slf4j
public class LoginController extends BaseController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private CountryService countryService;
    // @Autowired
    // private MemberEvent memberEvent;
    @Autowired
    private LocaleMessageSourceService messageSourceService;
    @Autowired
    private RedisTemplate redisTemplate;
    // @Autowired
    // private LocaleMessageSourceService msService;
    @Autowired
    private GeetestLib gtSdk;

    // @Autowired
    // private RegistrationTokenUtil registrationTokenUtil;

    @Autowired
    private GoogleOAuthConfig googleConfig;

    @Autowired
    private AppleOAuthConfig appleConfig;

    @Autowired
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private MemberEvent memberEvent;

    @Autowired
    private EmailService emailService;

    @Value("${spring.security.2fa.ttl}")
    private long secondAuthTtl;

    @Value("${person.promote.prefix:}")
    private String promotePrefix;

    private String sKey = "ab2cc473d3334c39";
    private String salt = "XPYQZb1kMES8HNaJWW8+TDu/4JdBK4owsU9eXCXZDOI=";

    @ApiOperation(value = "Login")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "Username"),
            @ApiImplicitParam(name = "password", value = "Password"),
            @ApiImplicitParam(name = "code", value = "Verification code"),
            @ApiImplicitParam(name = "country", value = "Country")

    })
    @RequestMapping(value = "/login")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult login(HttpServletRequest request, HttpServletResponse response, String username,
            String password, String country,
            Long expiredDays)
            throws Exception {
        Assert.hasText(username, messageSourceService.getMessage("MISSING_USERNAME"));
        Assert.hasText(password, messageSourceService.getMessage("MISSING_PASSWORD"));
        String ip = IPUtils.getIpAddr(request);
        String normalizedUsername = EmailUtil.normalize(username);

        // Check if user exists in database
        Member member = memberService.findByEmail(normalizedUsername);
        if (member == null) {
            return error(messageSourceService.getMessage("ACCOUNT_DOES_NOT_EXIST"));
        }

        // check login 5 , then account locked.
        String failKey = "LOGIN_FAIL_COUNT_" + normalizedUsername;
        BoundValueOperations<String, Integer> failOps = redisTemplate.boundValueOps(failKey);
        Integer failCount = failOps.get() != null ? failOps.get() : 0;

        if (failCount >= 5) {
            Long lockTimeMs = redisTemplate.getExpire(failKey, TimeUnit.MILLISECONDS);
            if (lockTimeMs != null && lockTimeMs > 0) {
                if (lockTimeMs > 60_000) {
                    long lockMinutes = (lockTimeMs + 59_999) / 1000 / 60;
                    return error(
                            messageSourceService.getMessage("ACCOUNT_LOCKED_REMAINING_TIME",
                                    new Object[] { lockMinutes }));
                } else {
                    // countdown in seconds
                    long lockSeconds = lockTimeMs / 1000;
                    return error(
                            messageSourceService.getMessage("ACCOUNT_LOCKED_REMAINING_TIME_SECONDS",
                                    new Object[] { lockSeconds }));
                }
            }
        }
        try {
            if (expiredDays == null || expiredDays <= 0) {
                expiredDays = 1L;
            }
            LoginInfo loginInfo = getLoginResult(request, response, username, password, ip, country, expiredDays);
            failOps.set(0);
            redisTemplate.expire(failKey, 24, TimeUnit.HOURS);
            return success(loginInfo);
        } catch (Exception e) {
            failCount += 1;
            failOps.set(failCount);

            if (failCount >= 5) {
                redisTemplate.expire(failKey, 1, TimeUnit.HOURS);
                return error(messageSourceService.getMessage("ACCOUNT_TEMPORARY_LOCKED"));
            } else {
                int remainingAttempts = 5 - failCount;
                redisTemplate.expire(failKey, 15, TimeUnit.MINUTES);
                return error(messageSourceService.getMessage("LOGIN_FAILED", new Object[] { remainingAttempts }));
            }
        }
    }

    private LoginInfo getLoginResult(HttpServletRequest request, HttpServletResponse response, String username,
            String password, String ip,
            String country,
            Long expiredDays) throws Exception {
        Member member = memberService.loginWithPassword(username, password, country);
        return postAuth(request, response, member, ip, country, expiredDays);
    }

    private LoginInfo postAuth(HttpServletRequest request, HttpServletResponse response, Member member, String ip,
            String country,
            Long expiredDays) throws Exception {
        if (member.getGoogleState() != null && member.getGoogleState().intValue() == 1) {
            String secondAuthToken = generateSecondAuthToken();
            String key = "SECOND_AUTH_TOKEN:" + secondAuthToken;

            Map<String, Object> challenge = new HashMap<>();
            challenge.put("memberId", member.getId());
            challenge.put("ipHash", MD5.md5(ip));
            challenge.put("uaHash", MD5.md5(String.valueOf(request.getHeader("User-Agent"))));
            challenge.put("createdAt", System.currentTimeMillis());

            redisTemplate.opsForValue().set(key, JSON.toJSONString(challenge), secondAuthTtl, TimeUnit.SECONDS);

            QueryWrapper<Country> cq = new QueryWrapper<>();
            cq.eq("vi_name", member.getLocal());
            Country countryEntry = countryService.getOne(cq);

            LoginInfo loginInfo = LoginInfo.getLoginInfo(
                    member,
                    countryEntry,
                    null, // token null vì chưa pass 2FA
                    false,
                    promotePrefix,
                    member.getLastLoginTime());

            loginInfo.setIsShowSecondAuth(true);
            loginInfo.setSecondAuthToken(secondAuthToken);
            return loginInfo;
        } else {
            // Không bật 2FA -> phát JWT như hiện tại
            return getLoginToken(response, member, ip, country, expiredDays);
        }
    }

    private LoginInfo getLoginToken(HttpServletResponse response, Member member, String ip, String country,
            Long expiredDays) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(SysConstant.SESSION_MEMBER, JSON.toJSONString(AuthMember.toAuthMember(member)));
        dataMap.put("roles", 1);
        dataMap.put("ip", ip);
        // Create token
        String token = JwtToken.createToken(dataMap, null, member.getId().toString());
        long maxAge = expiredDays * 24 * 60 * 60;
        String refreshToken = JwtToken.createRefreshToken(null, member.getId().toString(), maxAge);
        // hash rToken with key
        Map<String, String> dataMapRToken = new HashMap<String, String>();
        dataMapRToken.put("rToken", refreshToken);
        Signature signature = new Signature(sKey, salt);
        String _rToken = signature.security(dataMapRToken);
        ResponseCookie refreshCookie = ResponseCookie.from("_rToken", _rToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, _rToken");
        response.setHeader("Cache-Control", "public");
        // Get last login
        Date lastLoginBefore = member.getLastLoginTime();
        member.setLastLoginTime(new Date());
        member.setLoginCount(member.getLoginCount() + 1);
        memberService.updateById(member);
        String key = SysConstant.TOKEN_MEMBER + member.getId() + "_" + ip;
        redisTemplate.boundValueOps(key).set(token);
        redisTemplate.expire(key, 15, TimeUnit.MINUTES);
        QueryWrapper<Country> cq = new QueryWrapper<>();
        cq.eq("zh_name", member.getLocal());
        Country countryEntry = countryService.getOne(cq);

        LoginInfo loginInfo = LoginInfo.getLoginInfo(member, countryEntry, token, false, promotePrefix,
                lastLoginBefore);
        loginInfo.setRefreshToken(_rToken);
        loginInfo.setIsShowSecondAuth(false);
        loginInfo.setSecondAuthToken(null);
        return loginInfo;
    }

    private String generateSecondAuthToken() {
        return "WEblogin" + java.util.UUID.randomUUID().toString().replace("-", "");
    }

    @PostMapping(value = "/auth/verify/2fa", consumes = "application/json")
    public MessageResult verifyGoogleCode(@RequestBody VerifyLogin2FA input, HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        Assert.hasText(input.getGoogleCode(), messageSourceService.getMessage("MISSING_GOOOGLE_CODE"));
        Assert.hasText(input.getSecondAuthToken(), messageSourceService.getMessage("MISSING_SECOND_AUTH_TOKEN"));

        String redisKey = "SECOND_AUTH_TOKEN:" + input.getSecondAuthToken();
        String raw = (String) redisTemplate.opsForValue().get(redisKey);
        if (raw == null) {
            return MessageResult.error(messageSourceService.getMessage("INFORMATION_EXPIRED"));
        }

        JSONObject challenge = JSONObject.parseObject(raw);

        // check IP
        String ip = IPUtils.getIpAddr(request);
        String ua = String.valueOf(request.getHeader("User-Agent"));
        String ipHash = challenge.getString("ipHash");
        String uaHash = challenge.getString("uaHash");

        if (ipHash != null && !ipHash.equals(MD5.md5(ip))) {
            return MessageResult.error("SECOND_AUTH_INVALID_CONTEXT");
        }
        if (uaHash != null && !uaHash.equals(MD5.md5(ua))) {
            return MessageResult.error("SECOND_AUTH_INVALID_CONTEXT");
        }

        // find member
        Long memberId = challenge.getLong("memberId");
        if (memberId == null) {
            redisTemplate.delete(redisKey);
            return MessageResult.error(messageSourceService.getMessage("INFORMATION_EXPIRED"));
        }

        Member member = memberService.getById(memberId);
        if (member == null) {
            redisTemplate.delete(redisKey);
            return MessageResult.error(messageSourceService.getMessage("ACCOUNT_DOES_NOT_EXIST"));
        }

        // verify TOTP
        long code;
        try {
            code = Long.parseLong(input.getGoogleCode());
        } catch (Exception e) {
            return MessageResult.error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
        }

        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        boolean checkSecondAuth = ga.check_code(member.getGoogleKey(), code, System.currentTimeMillis());
        if (!checkSecondAuth) {
            return MessageResult.error(messageSourceService.getMessage("GOOGLE_VERIFICATION_CODE_ERROR"));
        }

        redisTemplate.delete(redisKey);

        Long expiredDays = input.getExpiredDays();
        if (expiredDays == null || expiredDays <= 0)
            expiredDays = 1L;

        LoginInfo loginInfo = getLoginToken(response, member, ip, member.getCountry(), expiredDays);

        MessageResult result = MessageResult.success();
        result.setData(loginInfo);
        return result;
    }

    /**
     * Logout
     *
     * @return
     */
    @ApiOperation(value = "Logout")
    @PermissionOperation
    @RequestMapping(value = "/loginout")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginOut(HttpServletRequest request, HttpServletResponse response,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        MessageResult messageResult = new MessageResult();
        String ip = IPUtils.getIpAddr(request);

        try {
            String key = SysConstant.TOKEN_MEMBER + user.getId() + "_" + ip;
            redisTemplate.expire(key, 1, TimeUnit.MILLISECONDS);

            // Clear refresh token cookie
            ResponseCookie clearCookie = ResponseCookie.from("_rToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(0)
                    .build();
            response.addHeader("Set-Cookie", clearCookie.toString());

            messageResult = success(messageSourceService.getMessage("LOGOUT_SUCCESS"));
        } catch (Exception e) {
            e.printStackTrace();
            messageResult = error(messageSourceService.getMessage("LOGOUT_FAILED"));

        }

        return messageResult;
    }

    /**
     * Check whether logged in
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "Check whether logged in")
    @PermissionOperation
    @RequestMapping("/check/login")
    public MessageResult checkLogin(HttpServletRequest request,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        MessageResult result = MessageResult.success();
        if (user != null) {
            result.setData(true);
        } else {
            result.setData(false);
        }
        return result;
    }

    /**
     * Get slider captcha image
     * 
     * @param request
     * @return
     */
    @ApiOperation(value = "Get slider captcha image")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "Username"),
            @ApiImplicitParam(name = "type", value = "Login method")
    })
    @RequestMapping(value = "/getYZMPic")
    public MessageResult getYZMPic(HttpServletRequest request, String username, String type) {
        ImageCaptchaResourceManager imageCaptchaResourceManager = new DefaultImageCaptchaResourceManager();
        ImageCaptchaGenerator imageCaptchaGenerator = new MultiImageCaptchaGenerator(imageCaptchaResourceManager)
                .init(true);
        /*
         * Generate slider captcha image, options:
         * SLIDER (Slider captcha)
         * ROTATE (Rotate captcha)
         * CONCAT (Drag-and-drop restore captcha)
         * WORD_IMAGE_CLICK (Text click captcha)
         * 
         * More captcha types see
         * cloud.tianai.captcha.common.constant.CaptchaTypeConstant
         */
        ImageCaptchaInfo imageCaptchaInfo = imageCaptchaGenerator.generateCaptchaImage(CaptchaTypeConstant.SLIDER);
        MessageResult result = MessageResult.success();
        Map<String, String> map = new HashMap<>();
        map.put("backgroundImage", imageCaptchaInfo.getBackgroundImage());
        map.put("sliderImage", imageCaptchaInfo.getSliderImage());
        result.setData(map);
        String key = "SLIDER_" + type + "_" + username;
        BoundValueOperations ops = redisTemplate.boundValueOps(key);
        Float percentage = Float.valueOf(imageCaptchaInfo.getRandomX())
                / (imageCaptchaInfo.getBgImageWidth() - imageCaptchaInfo.getSliderImageWidth());
        ops.set(percentage);

        return result;
    }

    /**
     * Verify slider captcha
     * 
     * @param request
     * @return
     */
    @ApiOperation(value = "Verify slider captcha")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "Username"),
            @ApiImplicitParam(name = "code", value = "Verification code"),
            @ApiImplicitParam(name = "type", value = "Login method")
    })
    @RequestMapping(value = "/checkYZMPic")
    public MessageResult checkYZMPic(HttpServletRequest request, String username, String code, String type) {
        String key = "SLIDER_" + type + "_" + username;
        BoundValueOperations ops = redisTemplate.boundValueOps(key);
        Object per = ops.get();
        if (per != null && !StringUtils.isEmpty(code)) {
            Float percentage = Float.valueOf(per.toString());
            ImageCaptchaValidator sliderCaptchaValidator = new BasicCaptchaTrackValidator();
            boolean check = sliderCaptchaValidator.checkPercentage(Float.valueOf(code), percentage, 0.1f);
            if (check) {
                String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
                ops.set(randomCode);
                MessageResult result = MessageResult.success();
                result.setData(randomCode);
                return result;
            }
        }
        return error(messageSourceService.getMessage("GEETEST_FAIL"));
    }

    @ApiOperation(value = "Login/Register with Google")
    @RequestMapping(value = "/loginWithGoogle", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginWithGoogle(@Valid @RequestBody LoginBySocial input, HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "lang", required = false) String lang)
            throws Exception {

        String idTokenString = input.getIdToken();
        // String googleCode = input.getGoogleCode();
        // String promotion = body.get("promotion");
        String ip = IPUtils.getIpAddr(request);

        NetHttpTransport transport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleConfig.getClientId()))
                .setIssuer("https://accounts.google.com")
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            return error(messageSourceService.getMessage("INVALID_GOOGLE_TOKEN"));
        }

        if (idToken == null) {
            return error(messageSourceService.getMessage("GOOGLE_TOKEN_NULL_OR_INVALID"));
        }
        Payload payload = idToken.getPayload();
        String email = EmailUtil.normalize(payload.getEmail());
        String sub = payload.getSubject(); // Google unique ID
        String fullName = (String) payload.get("name");

        Boolean emailVerified = (Boolean) payload.getEmailVerified();
        if (emailVerified == null || !emailVerified) {
            return error(messageSourceService.getMessage("GOOGLE_EMAIL_NOT_VERIFIED"));
        }

        if (!StringUtils.hasText(email)) {
            return error(messageSourceService.getMessage("GOOGLE_TOKEN_MISSING_EMAIL"));
        }

        Member memberBySub = memberService.findByGoogleSub(sub);
        if (memberBySub != null) {
            // get login info
            LoginInfo loginInfo = postAuth(request, response, memberBySub, ip, memberBySub.getCountry(), 1L);
            return success(loginInfo);
        } else {
            // check email
            Member memberByEmail = memberService.findByEmail(email);
            if (memberByEmail != null) {
                // link gg sub and login
                memberByEmail.setGoogleSub(sub);
                memberService.updateById(memberByEmail);
                LoginInfo loginInfo = postAuth(request, response, memberByEmail, ip, memberByEmail.getCountry(),
                        1L);
                return success(loginInfo);
            } else {
                Member member = new Member();
                member.setEmail(email);
                member.setUsername(email);
                member.setRealName(fullName);
                member.setGoogleSub(sub);
                member.setLoginType(LoginTypeEnum.GOOGLE);
                member.setHasPassword(0);
                // member.setPassword(password);
                member.setMemberLevel(MemberLevelEnum.GENERAL.getCode());
                member.setAvatar(
                        "https://wikex-exchange.sgp1.digitaloceanspaces.com/e679538f-fa4d-4f7d-899d-8d4dd48774ed.png");
                String code = BitShiftUniqueCodeGenerator.generateUniqueCode();
                member.setPromotionCode(code);
                member.setUid(code.substring(code.length() - 9, code.length()));
                memberService.save(member);
                memberEvent.onRegisterSuccess(member, null, lang);
                LoginInfo loginInfo = postAuth(request, response, member, ip, member.getCountry(), 1L);
                JSONObject responseData = new JSONObject();
                responseData.put("loginInfo", loginInfo);
                responseData.put("newUser", true);
                MessageResult result = MessageResult.success();
                result.setData(responseData);
                return result;
            }
        }
    }

    @ApiOperation(value = "Login/Register with Apple ID")
    @RequestMapping(value = "/loginWithApple", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginWithApple(@Valid @RequestBody LoginBySocial input, HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "lang", required = false) String lang)
            throws Exception {
        String identityToken = input.getIdToken();
        // String googleCode = input.getGoogleCode();
        String ip = IPUtils.getIpAddr(request);

        if (!StringUtils.hasText(identityToken)) {
            return error(messageSourceService.getMessage("MISSING_APPLE_IDENTITY_TOKEN"));
        }
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(identityToken);
        } catch (ParseException e) {
            return error(messageSourceService.getMessage("INVALID_APPLE_TOKEN_FORMAT"));
        }

        JWKSet publicKeys = JWKSet.load(new URL("https://appleid.apple.com/auth/keys"));
        JWKSelector selector = new JWKSelector(JWKMatcher.forJWSHeader(signedJWT.getHeader()));
        JWK jwk = selector.select(publicKeys).get(0);

        RSAKey rsaKey = (RSAKey) jwk;
        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

        boolean isValid = signedJWT.verify(new RSASSAVerifier(publicKey));
        if (!isValid) {
            return error(messageSourceService.getMessage("INVALID_APPLE_TOKEN_SIGNATURE"));
        }
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String email = EmailUtil.normalize(claims.getStringClaim("email"));
        String sub = claims.getSubject(); // Apple unique user ID

        if (!claims.getAudience().contains(appleConfig.getClientId())) {
            return error(messageSourceService.getMessage("INVALID_APPLE_AUDIENCE"));
        }

        if (!StringUtils.hasText(email)) {
            return error(messageSourceService.getMessage("APPLE_TOKEN_MISSING_EMAIL"));
        }
        Member memberBySub = memberService.findByAppleSub(sub);

        if (memberBySub != null) {
            // get login info
            LoginInfo loginInfo = postAuth(request, response, memberBySub, ip, memberBySub.getCountry(), 1L);
            return success(loginInfo);
        } else {
            Member memberByEmail = memberService.findByEmail(email);
            if (memberByEmail != null) {
                // link apple sub and login
                memberByEmail.setAppleSub(sub);
                memberService.updateById(memberByEmail);
                LoginInfo loginInfo = postAuth(request, response, memberByEmail, ip, memberByEmail.getCountry(), 1L);
                return success(loginInfo);
            } else {
                return error(messageSourceService.getMessage("APPLE_ID_NOT_LINKED_PLEASE_REGISTER"));
            }
        }
    }

    @ApiOperation(value = "check Google Authenticator status")
    @GetMapping("/ga-check")
    public MessageResult getMethodName(@RequestParam String username) {
        Member member = memberService.findByUsername(username);
        JSONObject response = new JSONObject();
        MessageResult responseResult = MessageResult.success();
        if (member == null) {
            response.put("gaEnabled", false);
            responseResult.setData(response);
            return responseResult;
        }
        response.put("gaEnabled", member.getGoogleState() != null && member.getGoogleState() == 1);
        responseResult.setData(response);
        // Add logic to check Google Authenticator status or code here
        return responseResult;
    }

    /**
     * Refresh access token using refresh token from cookie
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return MessageResult with new access token
     */
    @GetMapping("/refresh/token")
    public MessageResult refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("Starting token refresh process...");
            // Get refresh token from cookie
            String refreshToken = null;
            Cookie[] cookies = request.getCookies();
            Signature signature = new Signature(sKey, salt);
            log.info("Retrieving refresh token from cookies: {}", cookies != null ? cookies : 0);
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("_rToken".equals(cookie.getName())) {
                        String refreshTokenHash = cookie.getValue();
                        // verify hash to get real rToken
                        Map<String, String> dataMapRToken = signature.security(refreshTokenHash);
                        refreshToken = dataMapRToken.get("rToken");
                        break;
                    }
                }
            }
            log.info("Refresh token retrieved: {}", refreshToken != null ? "present" : "not present");
            if (refreshToken == null || refreshToken.isEmpty()) {
                // find from header
                String refreshTokenHash = request.getHeader("rToken");
                if (refreshTokenHash == null || refreshTokenHash.isEmpty()) {
                    return error(messageSourceService.getMessage("REFRESH_TOKEN_NOT_FOUND"));
                } else {
                    Map<String, String> dataMapRToken = signature.security(refreshTokenHash);
                    refreshToken = dataMapRToken.get("rToken");
                }
            }

            // Parse and validate refresh token
            Map<String, Object> tokenData = JwtToken.parseToken(refreshToken);
            log.info("Refresh token parsed data: {}", tokenData);
            if (tokenData == null) {
                return error(messageSourceService.getMessage("INVALID_REFRESH_TOKEN"));
            }
            log.info("Refresh token parsed successfully: {}", tokenData);
            // Get member ID from token subject
            String memberId = (String) tokenData.get("memberId");

            if (memberId == null || memberId.isEmpty()) {
                return error(messageSourceService.getMessage("INVALID_TOKEN_DATA"));
            }

            // Verify token exists in Redis
            String redisKey = SysConstant.TOKEN_MEMBER + memberId + "_" + IPUtils.getIpAddr(request);

            // Get member information from Feign
            Member member = memberService.findById(Long.parseLong(memberId));
            if (member == null) {
                return error(messageSourceService.getMessage("MEMBER_NOT_EXISTS"));
            }
            log.info("Member found: {}", member.getEmail());
            // Generate new access token
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(SysConstant.SESSION_MEMBER, JSON.toJSONString(AuthMember.toAuthMember(member)));
            dataMap.put("roles", 1);
            dataMap.put("ip", IPUtils.getIpAddr(request));

            String newAccessToken = JwtToken.createToken(dataMap, null, memberId);

            // Update token in Redis
            redisTemplate.boundValueOps(redisKey).set(newAccessToken);
            redisTemplate.expire(redisKey, 15, TimeUnit.MINUTES);

            // Return new access token
            Map<String, Object> result = new HashMap<>();
            result.put("token", newAccessToken);
            result.put("memberId", memberId);
            log.info("Token refresh successful for memberId: {}", memberId);
            return success(result);
        } catch (Exception e) {
            log.error("Refresh token error: ", e);
            return error(messageSourceService.getMessage("REFRESH_TOKEN_FAILED"));
        }
    }
}
