package com.wikex.wikex.coinswap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.mapper.ContractCoinCoinMapper;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class ContractCoinCoinServiceImpl extends ServiceImpl<ContractCoinCoinMapper, ContractCoinCoin> implements ContractCoinCoinService {

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
    public ContractCoinCoin findBySymbol(String symbol) {
        QueryWrapper<ContractCoinCoin> queryWrapper = new QueryWrapper<>();
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
    public List<ContractCoinCoin> findAllVisible() {
        QueryWrapper<ContractCoinCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ContractCoinCoin::getVisible,1).eq(ContractCoinCoin::getEnable,1).orderByAsc(ContractCoinCoin::getSort);
        return list(queryWrapper);
    }

    @Override
    public List<ContractCoinCoin> findAllEnabled() {
        QueryWrapper<ContractCoinCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ContractCoinCoin::getEnable,1).orderByAsc(ContractCoinCoin::getSort);
        return list(queryWrapper);
    }

    @Override
    public Page<ContractCoinCoin> findAll(PageParam pageParam) {
        Page<ContractCoinCoin> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<ContractCoinCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sort");
        return this.page(page,queryWrapper);
    }

    
    public void savePoke(String symbol, BigDecimal price){
        Poke poke = new Poke(price.stripTrailingZeros().toPlainString());
        for (String period : periods) {
            mongoTemplate.insert(poke,"contract_poke_"+symbol+"_"+period+"_kline");
        }
//        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_depth");
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_detail");
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_trade");
    }
}
