package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.service.CountryService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Api(tags = "Verification Code")
@Slf4j
@RestController
@RequestMapping("/mobile")
public class SmsController extends BaseController {

    @Autowired
    private SMSProvider smsProvider;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberService memberService;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;
    @Autowired
    private CountryService countryService;

    @ApiOperation(value = "Send Registration Verification Code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "country", value = "Country Information"),
            @ApiImplicitParam(name = "phone", value = "Phone Number"),
            @ApiImplicitParam(name = "code", value = "Verification Code"),
            @ApiImplicitParam(name = "type", value = "Verification Method")
    })
    @PostMapping("/code")
    public MessageResult sendCheckCode(String phone, String country, String type, String code) throws Exception {
        Assert.isTrue(!memberService.phoneIsExist(phone), localeMessageSourceService.getMessage("PHONE_ALREADY_EXISTS"));
        Assert.notNull(country, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
        Country country1 = countryService.findOne(country);
        Assert.notNull(country1, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));

        int gtResult = 0;
        String key = "SLIDER_" + type + "_" + phone;
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

        ValueOperations valueOperations = redisTemplate.opsForValue();
        String smsKey = SysConstant.PHONE_REG_CODE_PREFIX + phone;
        Object smsCode = valueOperations.get(smsKey);
        if (smsCode != null) {
            // If request interval is less than one minute, request fails
            if (!BigDecimalUtils.compare(DateUtil.diffMinute((Date) (valueOperations.get(smsKey + "Time"))), BigDecimal.ONE)) {
                return error(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
            }
        }

        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        MessageResult result;
        if ("86".equals(country1.getAreaCode())) {
            Assert.isTrue(ValidateUtil.isMobilePhone(phone.trim()), localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            result = smsProvider.sendVerifyMessage(phone, randomCode);
        } else {
            result = smsProvider.sendInternationalMessage(randomCode, country1.getAreaCode() + phone);
        }
        if (result.getCode() == 0) {
            valueOperations.getOperations().delete(smsKey);
            valueOperations.getOperations().delete(smsKey + "Time");
            // Cache verification code
            valueOperations.set(smsKey, randomCode, 10, TimeUnit.MINUTES);
            valueOperations.set(smsKey + "Time", new Date(), 10, TimeUnit.MINUTES);
            return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("SEND_SMS_FAILED"));
        }
    }

    @ApiOperation(value = "Reset Transaction Password Verification Code")
    @PermissionOperation
    @RequestMapping(value = "/transaction/code", method = RequestMethod.POST)
    public MessageResult sendResetTransactionCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return sendCodeMessage(authMember, SysConstant.PHONE_RESET_TRANS_CODE_PREFIX);
    }

    @ApiOperation(value = "Bind Phone Verification Code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "country", value = "Country Information"),
            @ApiImplicitParam(name = "phone", value = "Phone Number"),
    })
    @PermissionOperation
    @RequestMapping(value = "/bind/code", method = RequestMethod.POST)
    public MessageResult setBindPhoneCode(String country, String phone, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        Assert.isNull(member.getMobilePhone(), localeMessageSourceService.getMessage("REPEAT_PHONE_REQUEST"));
        MessageResult result;
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));

        // Update country info
        Country one = null;
        if (StringUtils.isNotBlank(country)) {
            one = countryService.findOne(country);
            if (one != null) {
                member.setLocal(one.getZhName());
                member.setCountry(one.getZhName());
                memberService.updateById(member);
            }
        } else {
            one = countryService.findOne(member.getLocal());
        }

        if ("86".equals(one.getAreaCode())) {
            if (!ValidateUtil.isMobilePhone(phone.trim())) {
                return error(localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            }
            result = smsProvider.sendVerifyMessage(phone, randomCode);
        } else {
            result = smsProvider.sendInternationalMessage(randomCode, one.getAreaCode() + phone);
        }
        if (result.getCode() == 0) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String key = SysConstant.PHONE_BIND_CODE_PREFIX + phone;
            valueOperations.getOperations().delete(key);
            // Cache verification code
            valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
            return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("SEND_SMS_FAILED"));
        }
    }

    @ApiOperation(value = "Update Login Password Verification Code")
    @PermissionOperation
    @RequestMapping(value = "/update/password/code", method = RequestMethod.POST)
    public MessageResult updatePasswordCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return this.sendCodeMessage(authMember, SysConstant.PHONE_UPDATE_PASSWORD_PREFIX);
    }

    @ApiOperation(value = "Address Verification Code")
    @PermissionOperation
    @RequestMapping(value = "/add/address/code", method = RequestMethod.POST)
    public MessageResult addAddressCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return this.sendCodeMessage(authMember, SysConstant.PHONE_ADD_ADDRESS_PREFIX);
    }

    @ApiOperation(value = "Withdraw Verification Code")
    @PermissionOperation
    @RequestMapping(value = "/activity/code", method = RequestMethod.POST)
    public MessageResult attendActivityCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return this.sendCodeMessage(authMember, SysConstant.PHONE_ATTEND_ACTIVITY_PREFIX);
    }

    @ApiOperation(value = "Verification Code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coin", value = "Coin Type"),
    })
    @PermissionOperation
    @RequestMapping(value = "/withdraw/code", method = RequestMethod.POST)
    public MessageResult withdrawCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return this.sendCodeMessage(authMember, SysConstant.PHONE_WITHDRAW_MONEY_CODE_PREFIX);
    }

    @ApiOperation(value = "CTC Verification Code")
    @PermissionOperation
    @RequestMapping(value = "/ctc/code", method = RequestMethod.POST)
    public MessageResult ctcCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return this.sendCodeMessage(authMember, SysConstant.PHONE_CTC_TRADE_CODE_PREFIX);
    }

    @ApiOperation(value = "Forgot Password Verification Code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "Account"),
    })
    @RequestMapping(value = "/reset/code", method = RequestMethod.POST)
    public MessageResult resetPasswordCode(String account) throws Exception {
        Member member = memberService.findByPhone(account);
        Assert.notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        return this.sendCodeMessage(member, SysConstant.RESET_PASSWORD_CODE_PREFIX);
    }

    @ApiOperation(value = "Change Phone Verification Code")
    @PermissionOperation
    @RequestMapping(value = "/change/code", method = RequestMethod.POST)
    public MessageResult resetPhoneCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return this.sendCodeMessage(authMember, SysConstant.PHONE_CHANGE_CODE_PREFIX);
    }

    @ApiOperation(value = "Bind API Send Verification Code")
    @PermissionOperation
    @RequestMapping(value = "api/code", method = RequestMethod.POST)
    public MessageResult bindApiSendCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) throws Exception {
        return this.sendCodeMessage(authMember, SysConstant.API_BIND_CODE_PREFIX);
    }

    private MessageResult sendCodeMessage(String authMember, String prefix) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        return sendCodeMessage(member, prefix);
    }

    private MessageResult sendCodeMessage(Member member, String prefix) throws Exception {
        Assert.hasText(member.getMobilePhone(), localeMessageSourceService.getMessage("NOT_BIND_PHONE"));
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        Country country = countryService.findOne(member.getLocal());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = prefix + member.getMobilePhone();
        Object code = valueOperations.get(key);
        if (code != null) {
            // If request interval is less than one minute, request fails
            if (!BigDecimalUtils.compare(DateUtil.diffMinute((Date) (valueOperations.get(key + "Time"))), BigDecimal.ONE)) {
                return error(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
            }
        }
        MessageResult result;
        if ("86".equals(country.getAreaCode())) {
            result = smsProvider.sendVerifyMessage(member.getMobilePhone(), randomCode);
        } else {
            result = smsProvider.sendInternationalMessage(randomCode, country.getAreaCode() + member.getMobilePhone());
        }
        if (result != null && result.getCode() == 0) {
            valueOperations.getOperations().delete(key);
            valueOperations.getOperations().delete(key + "Time");
            // Cache verification code
            valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
            valueOperations.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
            return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("SEND_SMS_FAILED"));
        }
    }
}
