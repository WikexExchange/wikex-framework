package com.wikex.wikex.coinswap.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.coinswap.entity.CoinswapFavorSymbol;
import com.wikex.wikex.coinswap.service.CoinswapFavorSymbolService;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Api(tags = "Favorite Trading Symbol Handling")
@Slf4j
@RestController
@RequestMapping("/favor")
public class CoinswapFavorController extends BaseController {
    @Autowired
    private CoinswapFavorSymbolService favorSymbolService;

    @Autowired
    private LocaleMessageSourceService msService;

    
    @ApiOperation(value = "Add Favorite")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @PermissionOperation
    @RequestMapping("add")
    public MessageResult addFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String symbol){
        AuthMember member = AuthMember.toAuthMember(authMember);
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error(500,msService.getMessage("SYMBOL_CANNOT_BE_EMPTY"));
        }
        CoinswapFavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(favorSymbol != null){
            return MessageResult.error(500,msService.getMessage("SYMBOL_ALREADY_FAVORED"));
        }
        CoinswapFavorSymbol favor =  favorSymbolService.add(member.getId(),symbol);
        if(favor!= null){
            return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
        }
        return MessageResult.error(msService.getMessage("EXAPI_ERROR"));
    }

    
    @ApiOperation(value = "Get Current User Favorites")
    @PermissionOperation
    @RequestMapping("find")
    public MessageResult findFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember){
        AuthMember member = AuthMember.toAuthMember(authMember);
        List<CoinswapFavorSymbol> list = favorSymbolService.findByMemberId(member.getId());
        return success(list);
    }

    
    @ApiOperation(value = "Delete Favorite")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @PermissionOperation
    @RequestMapping("delete")
    public MessageResult deleteFavor(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,String symbol){
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error(msService.getMessage("SYMBOL_CANNOT_BE_EMPTY"));
        }
        AuthMember member = AuthMember.toAuthMember(authMember);
        CoinswapFavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(favorSymbol == null){
            return MessageResult.error(msService.getMessage("FAVOR_NOT_EXISTS"));
        }
        favorSymbolService.delete(member.getId(),symbol);
        return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
    }
}
