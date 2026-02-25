package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.swap.entity.SwapFavorSymbol;

import java.util.List;


public interface SwapFavorSymbolService extends IService<SwapFavorSymbol> {
    
    public SwapFavorSymbol add(Long memberId, String symbol);

    
    public void delete(Long memberId,String symbol);

    
    public List<SwapFavorSymbol> findByMemberId(Long memberId);
    
    public SwapFavorSymbol findByMemberIdAndSymbol(Long memberId,String symbol);
}
