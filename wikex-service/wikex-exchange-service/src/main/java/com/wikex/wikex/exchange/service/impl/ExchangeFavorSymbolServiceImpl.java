package com.wikex.wikex.exchange.service.impl;

import com.wikex.wikex.exchange.entity.ExchangeFavorSymbol;
import com.wikex.wikex.exchange.mapper.ExchangeFavorSymbolMapper;
import com.wikex.wikex.exchange.service.ExchangeFavorSymbolService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ExchangeFavorSymbolServiceImpl extends ServiceImpl<ExchangeFavorSymbolMapper, ExchangeFavorSymbol> implements ExchangeFavorSymbolService {

    
    @Override
    public ExchangeFavorSymbol add(Long memberId,String symbol){
        ExchangeFavorSymbol favor = new ExchangeFavorSymbol();
        favor.setMemberId(memberId);
        favor.setAddTime(DateUtil.getDateTime());
        favor.setSymbol(symbol);
        this.save(favor);
        return favor;
    }

    
    @Override
    public void delete(Long memberId,String symbol){
        ExchangeFavorSymbol favor = this.baseMapper.findByMemberIdAndSymbol(memberId,symbol);
        if(favor != null){
            this.removeById(favor.getId());
        }
    }

    
    @Override
    public List<ExchangeFavorSymbol> findByMemberId(Long memberId){
        return this.baseMapper.findAllByMemberId(memberId);
    }

    
    @Override
    public ExchangeFavorSymbol findByMemberIdAndSymbol(Long memberId,String symbol){
        return this.baseMapper.findByMemberIdAndSymbol(memberId,symbol);
    }
}
