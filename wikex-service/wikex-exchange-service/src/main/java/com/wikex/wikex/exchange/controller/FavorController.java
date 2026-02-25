package com.wikex.wikex.exchange.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exchange.entity.ExchangeFavorSymbol;
import com.wikex.wikex.exchange.service.ExchangeFavorSymbolService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Api(tags = "Favorite trading pair handler")
@Slf4j
@RestController
@RequestMapping("/favor")
public class FavorController extends BaseController {
    @Autowired
    private ExchangeFavorSymbolService favorSymbolService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String addRedisKey = "FAVOR_EXCHANGE_ADD_%s_%s";

    /**
     * Add to favorites
     * @param authMember user session info
     * @param symbol trading pair symbol
     * @return result
     */
    @ApiOperation(value = "Add to favorites")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @PermissionOperation
    @RequestMapping("add")
    public MessageResult addFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String symbol){
        AuthMember member = AuthMember.toAuthMember(authMember);
        // acquire redis lock
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(addRedisKey, member.getId(), symbol);
        String redisVal = ops.get(key);
        if(redisVal != null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key, "11", 3, TimeUnit.MINUTES); // lock for 3 minutes

        if(StringUtils.isEmpty(symbol)){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SYMBOL_CANNOT_BE_EMPTY"));
        }

        ExchangeFavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(), symbol);
        if(favorSymbol != null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SYMBOL_ALREADY_FAVORED"));
        }
        ExchangeFavorSymbol favor = favorSymbolService.add(member.getId(), symbol);
        if(favor != null){
            redisTemplate.delete(key);
            return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
        }
        redisTemplate.delete(key);
        return MessageResult.error(msService.getMessage("EXAPI_ERROR"));
    }

    /**
     * Query current user's favorites
     * @param authMember user session info
     * @return list of favorite trading pairs
     */
    @ApiOperation(value = "Query current user's favorites")
    @PermissionOperation
    @RequestMapping("find")
    public MessageResult findFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember){
        AuthMember member = AuthMember.toAuthMember(authMember);
        List<ExchangeFavorSymbol> list = favorSymbolService.findByMemberId(member.getId());
        return this.success(list);
    }

    /**
     * Remove from favorites
     * @param authMember user session info
     * @param symbol trading pair symbol
     * @return result
     */
    @ApiOperation(value = "Remove from favorites")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @PermissionOperation
    @RequestMapping("delete")
    public MessageResult deleteFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String symbol){
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error(msService.getMessage("SYMBOL_CANNOT_BE_EMPTY"));
        }
        AuthMember member = AuthMember.toAuthMember(authMember);
        ExchangeFavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(), symbol);
        if(favorSymbol == null){
            return MessageResult.error(msService.getMessage("FAVOR_NOT_EXISTS"));
        }
        favorSymbolService.delete(member.getId(), symbol);
        return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
    }
}
