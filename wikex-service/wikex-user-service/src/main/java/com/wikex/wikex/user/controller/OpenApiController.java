package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.MemberApiKey;
import com.wikex.wikex.user.service.MemberApiKeyService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.util.RedisUtil;
import com.wikex.wikex.util.GeneratorUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("open")
public class OpenApiController extends BaseController {

    @Autowired
    private MemberApiKeyService memberApiKeyService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * Get ApiKey
     * 
     * @param authMember
     * @return
     */
    @PermissionOperation
    @RequestMapping(value = "get_key", method = RequestMethod.GET)
    public MessageResult queryApiKey(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        List<MemberApiKey> result = memberApiKeyService.findAllByMemberId(member.getId());
        return success(result);
    }

    /**
     * Add ApiKey
     * 
     * @param authMember
     * @param memberApiKey
     * @return
     */
    @PermissionOperation
    @RequestMapping(value = "api/save", method = RequestMethod.POST)
    public MessageResult saveApiKey(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            MemberApiKey memberApiKey) {
        AuthMember member = AuthMember.toAuthMember(authMember);

        String code = memberApiKey.getCode();
        Assert.isTrue(StringUtils.isNotEmpty(code), msService.getMessage("VERIFICATION_CODE_REQUIRED"));
        Object cacheCode = redisUtil.get(SysConstant.API_BIND_CODE_PREFIX + member.getMobilePhone());
        if (cacheCode == null) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_EXPIRED"));
        }
        if (!code.equalsIgnoreCase(cacheCode.toString())) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }
        List<MemberApiKey> all = memberApiKeyService.findAllByMemberId(member.getId());
        if (all.isEmpty() || all.size() < 5) {
            memberApiKey.setId(null);
            if (StringUtils.isBlank(memberApiKey.getBindIp())) {
                // If not binding IP, default expiration is 90 days
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 90);
                memberApiKey.setExpireTime(calendar.getTime());
            }
            memberApiKey.setApiName(member.getId() + "");
            memberApiKey.setApiKey(GeneratorUtil.getUUID());
            String secret = GeneratorUtil.getUUID();
            memberApiKey.setSecretKey(secret);
            memberApiKey.setMemberId(member.getId());
            memberApiKey.setCreateTime(new Date());
            memberApiKeyService.saveOrUpdate(memberApiKey);
            redisUtil.delete(SysConstant.API_BIND_CODE_PREFIX + member.getMobilePhone());
            return success(msService.getMessage("ADD_SUCCESSFUL"), secret);
        } else {
            return error(msService.getMessage("NUMBER_EXCEEDS_MAX"));
        }
    }

    /**
     * Update API-key
     * 
     * @param authMember
     * @param memberApiKey
     * @return
     */
    @PermissionOperation
    @RequestMapping(value = "api/update", method = RequestMethod.POST)
    public MessageResult updateApiKey(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            MemberApiKey memberApiKey) {
        AuthMember member = AuthMember.toAuthMember(authMember);

        if (memberApiKey.getId() != null) {
            MemberApiKey findMemberApiKey = memberApiKeyService.findByMemberIdAndId(member.getId(),
                    memberApiKey.getId());
            if (findMemberApiKey != null) {
                if (!memberApiKey.getRemark().equals(findMemberApiKey.getRemark())) {
                    findMemberApiKey.setRemark(memberApiKey.getRemark());
                }
                if (StringUtils.isNotEmpty(memberApiKey.getBindIp())) {
                    findMemberApiKey.setBindIp(memberApiKey.getBindIp());
                } else {
                    findMemberApiKey.setBindIp(null);
                }

                memberApiKeyService.saveOrUpdate(findMemberApiKey);
                return success(msService.getMessage("UPDATE_SUCCESSFUL"));
            } else {
                return error(msService.getMessage("RECORD_NOT_FOUND"));
            }
        } else {
            return error(msService.getMessage("RECORD_NOT_FOUND"));
        }
    }

    /**
     * Delete API-key
     * 
     * @param authMember
     * @param id
     * @return
     */
    @PermissionOperation
    @RequestMapping(value = "api/del/{id}", method = RequestMethod.GET)
    public MessageResult updateApiKey(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @PathVariable("id") Long id) {
        AuthMember member = AuthMember.toAuthMember(authMember);

        MemberApiKey memberApiKey = memberApiKeyService.findByMemberIdAndId(member.getId(), id);
        if (memberApiKey != null) {
            memberApiKeyService.removeById(id);
        } else {
            return error(msService.getMessage("RECORD_NOT_FOUND"));
        }

        return success(msService.getMessage("DELETE_SUCCESSFUL"));
    }
}
