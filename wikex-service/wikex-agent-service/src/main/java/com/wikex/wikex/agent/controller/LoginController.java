package com.wikex.wikex.agent.controller;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.CountryFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.LoginInfo;
import com.wikex.wikex.util.JwtToken;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class LoginController extends BaseController {

    @Autowired
    private MemberFeign memberFeign;

    @Autowired
    private CountryFeign countryFeign;

    @Autowired
    private LocaleMessageSourceService msService;

    @Value("${person.promote.prefix:}")
    private String promotePrefix;

    @Value("${spark.system.md5.key}")
    private String md5Key;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Login API
     *
     * @param request  Http request
     * @param username Username
     * @param password Password
     * @return login result with token
     */
    @RequestMapping(value = "/login")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult login(HttpServletRequest request, String username, String password) {
        Assert.hasText(username, msService.getMessage("MISSING_USERNAME"));
        Assert.hasText(password, msService.getMessage("MISSING_PASSWORD"));
        String ip = getRemoteIp(request);

        try {
            LoginInfo loginInfo = getLoginInfo(username, password, ip, request);
            return success(loginInfo);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private LoginInfo getLoginInfo(String username, String password, String ip, HttpServletRequest request)
            throws Exception {
        Member member = memberFeign.login(username, password);
        // Wrap token information
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(SysConstant.SESSION_MEMBER, JSON.toJSONString(AuthMember.toAuthMember(member)));
        dataMap.put("roles", 1);
        // Get IP
        dataMap.put("ip", MD5.md5(ip));
        // Create token
        String token = JwtToken.createToken(dataMap, null, member.getId().toString());
        // Save token to Redis
        String key = SysConstant.TOKEN_MEMBER + member.getId();
        redisTemplate.boundValueOps(key).set(token);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        Country country = countryFeign.findByZhName(member.getLocal());
        Date lastLoginBefore = member.getLastLoginTime();
        LoginInfo loginInfo = LoginInfo.getLoginInfo(member, country, token, false, promotePrefix, lastLoginBefore);
        return loginInfo;
    }

    /**
     * Logout
     *
     * @return MessageResult
     */
    @PermissionOperation
    @RequestMapping(value = "/loginout")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginOut(HttpServletRequest request,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        MessageResult messageResult;

        try {
            String key = SysConstant.TOKEN_MEMBER + user.getId();
            redisTemplate.expire(key, 1, TimeUnit.MILLISECONDS);
            messageResult = success(msService.getMessage("LOGOUT_SUCCESS"));
        } catch (Exception e) {
            e.printStackTrace();
            messageResult = error(msService.getMessage("LOGOUT_FAILED"));

        }

        return messageResult;
    }

    /**
     * Check if user is logged in
     *
     * @param request    HttpServletRequest
     * @param authMember Authorization token from header
     * @return login status
     */
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
}
