package com.wikex.wikex.swap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.mapper.ContractCoinMapper;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class ContractCoinServiceImpl extends ServiceImpl<ContractCoinMapper, ContractCoin> implements ContractCoinService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private List<String> periods = new ArrayList<String>(){{
        add("1min");
        add("5min");
        add("15min");
        add("30min");
        add("60min");
        add("4hour");
        add("1day");
        add("1week");
    }};

    @Override
    public List<String> getBaseSymbol() {
        return baseMapper.getBaseSymbol();
    }

    @Override
    public ContractCoin findBySymbol(String symbol) {
        QueryWrapper<ContractCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("symbol",symbol);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void increaseTotalOpenFee(Long id, BigDecimal amount) {
        baseMapper.increaseOpenFee(id, amount);
    }

    @Override
    public void increaseTotalCloseFee(Long id, BigDecimal amount) {
        baseMapper.increaseCloseFee(id, amount);
    }

    @Override
    public void increaseTotalLoss(Long id, BigDecimal amount) {
        baseMapper.increaseTotalLoss(id, amount);
    }

    @Override
    public void increaseTotalProfit(Long id, BigDecimal amount) {
        baseMapper.increaseTotalProfit(id, amount);
    }

    @Override
    public List<ContractCoin> findAllVisible() {
        QueryWrapper<ContractCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ContractCoin::getVisible,1).eq(ContractCoin::getEnable,1).orderByAsc(ContractCoin::getSort);
        return list(queryWrapper);
    }

    @Override
    public List<ContractCoin> findAllEnabled() {
        QueryWrapper<ContractCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ContractCoin::getEnable,1).orderByAsc(ContractCoin::getSort);
        return list(queryWrapper);
    }

    @Override
    public Page<ContractCoin> findAll(PageParam pageParam) {
        Page<ContractCoin> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<ContractCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sort");
        return this.page(page,queryWrapper);
    }

    
    @Override
    public void savePoke(String symbol, BigDecimal price){
        Poke poke = new Poke(price.stripTrailingZeros().toPlainString());
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_kline");
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_depth");
        for (String period : periods) {
            mongoTemplate.insert(poke,"contract_poke_"+symbol+"_"+period+"_kline");
        }
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_detail");
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_trade");
    }
}
