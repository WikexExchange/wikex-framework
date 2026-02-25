package com.wikex.wikex.swap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.swap.entity.SwapFavorSymbol;
import com.wikex.wikex.swap.mapper.SwapFavorSymbolMapper;
import com.wikex.wikex.swap.service.SwapFavorSymbolService;
import com.wikex.wikex.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SwapFavorSymbolServiceImpl extends ServiceImpl<SwapFavorSymbolMapper, SwapFavorSymbol> implements SwapFavorSymbolService {

    
    @Override
    public SwapFavorSymbol add(Long memberId,String symbol){
        SwapFavorSymbol favor = new SwapFavorSymbol();
        favor.setMemberId(memberId);
        favor.setAddTime(DateUtil.getDateTime());
        favor.setSymbol(symbol);
        this.save(favor);
        return favor;
    }

    
    @Override
    public void delete(Long memberId,String symbol){
        SwapFavorSymbol favor = this.baseMapper.findByMemberIdAndSymbol(memberId,symbol);
        if(favor != null){
            this.removeById(favor.getId());
        }
    }

    
    @Override
    public List<SwapFavorSymbol> findByMemberId(Long memberId){
        return this.baseMapper.findAllByMemberId(memberId);
    }

    
    @Override
    public SwapFavorSymbol findByMemberIdAndSymbol(Long memberId,String symbol){
        return this.baseMapper.findByMemberIdAndSymbol(memberId,symbol);
    }
}
