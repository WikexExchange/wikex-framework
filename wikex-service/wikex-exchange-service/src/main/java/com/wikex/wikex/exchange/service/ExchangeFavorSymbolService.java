package com.wikex.wikex.exchange.service;

import com.wikex.wikex.exchange.entity.ExchangeFavorSymbol;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.util.DateUtil;

import java.util.List;


public interface ExchangeFavorSymbolService extends IService<ExchangeFavorSymbol> {
    
    public ExchangeFavorSymbol add(Long memberId,String symbol);

    
    public void delete(Long memberId,String symbol);

    
    public List<ExchangeFavorSymbol> findByMemberId(Long memberId);
    
    public ExchangeFavorSymbol findByMemberIdAndSymbol(Long memberId,String symbol);
}
