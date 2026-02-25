package com.wikex.wikex.coinswap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.coinswap.entity.CoinswapFavorSymbol;

import java.util.List;


public interface CoinswapFavorSymbolService extends IService<CoinswapFavorSymbol> {
    
    public CoinswapFavorSymbol add(Long memberId, String symbol);

    
    public void delete(Long memberId,String symbol);

    
    public List<CoinswapFavorSymbol> findByMemberId(Long memberId);
    
    public CoinswapFavorSymbol findByMemberIdAndSymbol(Long memberId,String symbol);
}
