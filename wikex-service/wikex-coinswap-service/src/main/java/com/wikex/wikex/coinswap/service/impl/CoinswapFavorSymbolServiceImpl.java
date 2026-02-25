package com.wikex.wikex.coinswap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.coinswap.entity.CoinswapFavorSymbol;
import com.wikex.wikex.coinswap.mapper.CoinswapFavorSymbolMapper;
import com.wikex.wikex.coinswap.service.CoinswapFavorSymbolService;
import com.wikex.wikex.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CoinswapFavorSymbolServiceImpl extends ServiceImpl<CoinswapFavorSymbolMapper, CoinswapFavorSymbol> implements CoinswapFavorSymbolService {

    
    @Override
    public CoinswapFavorSymbol add(Long memberId,String symbol){
        CoinswapFavorSymbol favor = new CoinswapFavorSymbol();
        favor.setMemberId(memberId);
        favor.setAddTime(DateUtil.getDateTime());
        favor.setSymbol(symbol);
        this.save(favor);
        return favor;
    }

    
    @Override
    public void delete(Long memberId,String symbol){
        CoinswapFavorSymbol favor = this.baseMapper.findByMemberIdAndSymbol(memberId,symbol);
        if(favor != null){
            this.removeById(favor.getId());
        }
    }

    
    @Override
    public List<CoinswapFavorSymbol> findByMemberId(Long memberId){
        return this.baseMapper.findAllByMemberId(memberId);
    }

    
    @Override
    public CoinswapFavorSymbol findByMemberIdAndSymbol(Long memberId,String symbol){
        return this.baseMapper.findByMemberIdAndSymbol(memberId,symbol);
    }
}
