package com.wikex.wikex.swap.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.SwapFavorSymbol;
import com.wikex.wikex.swap.service.SwapFavorSymbolService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Api(tags = "Trading Favorite Symbol Management")
@Slf4j
@RestController
@RequestMapping("/favor")
public class SwapFavorController extends BaseController {
    @Autowired
    private SwapFavorSymbolService favorSymbolService;

    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * Add to favorites
     * @param authMember
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Add to favorites")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
    })
    @PermissionOperation
    @RequestMapping("add")
    public MessageResult addFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String symbol){
        AuthMember member = AuthMember.toAuthMember(authMember);
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error(500,msService.getMessage("SYMBOL_CANNOT_BE_EMPTY"));
        }
        SwapFavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(favorSymbol != null){
            return MessageResult.error(500,msService.getMessage("SYMBOL_ALREADY_FAVORED"));
        }
        SwapFavorSymbol favor =  favorSymbolService.add(member.getId(),symbol);
        if(favor!= null){
            return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
        }
        return MessageResult.error(msService.getMessage("EXAPI_ERROR"));
    }

    /**
     * Query current user's favorites
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Query current user's favorites")
    @PermissionOperation
    @RequestMapping("find")
    public MessageResult findFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember){
        AuthMember member = AuthMember.toAuthMember(authMember);
        List<SwapFavorSymbol> list =  favorSymbolService.findByMemberId(member.getId());
        return this.success(list);
    }

    /**
     * Delete from favorites
     * @param authMember
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Delete from favorites")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
    })
    @PermissionOperation
    @RequestMapping("delete")
    public MessageResult deleteFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,String symbol){
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error(msService.getMessage("SYMBOL_CANNOT_BE_EMPTY"));
        }
        AuthMember member = AuthMember.toAuthMember(authMember);
        SwapFavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(favorSymbol == null){
            return MessageResult.error(msService.getMessage("FAVOR_NOT_EXISTS"));
        }
        favorSymbolService.delete(member.getId(),symbol);
        return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
    }
}
