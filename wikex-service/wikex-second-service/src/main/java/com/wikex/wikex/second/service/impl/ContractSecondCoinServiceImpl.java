package com.wikex.wikex.second.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.mapper.ContractSecondCoinMapper;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class ContractSecondCoinServiceImpl extends ServiceImpl<ContractSecondCoinMapper, ContractSecondCoin> implements ContractSecondCoinService {

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
    private static String collectionNameKey = "contract_second_";

    @Override
    public List<ContractSecondCoin> findAllEnabled() {
        LambdaQueryWrapper<ContractSecondCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractSecondCoin::getEnable,1);
        queryWrapper.orderByAsc(ContractSecondCoin::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public ContractSecondCoin findBySymbol(String symbol) {
        LambdaQueryWrapper<ContractSecondCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractSecondCoin::getSymbol,symbol);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<String> getBaseSymbol() {
        return this.baseMapper.getBaseSymbol();
    }

    @Override
    public List<ContractSecondCoin> findAllVisible() {
        LambdaQueryWrapper<ContractSecondCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractSecondCoin::getEnable,1);
        queryWrapper.eq(ContractSecondCoin::getVisible,1);
        queryWrapper.orderByAsc(ContractSecondCoin::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public void savePoke(String symbol, BigDecimal price) {
        Poke poke = new Poke(price.stripTrailingZeros().toPlainString());
        for (String period : periods) {
            mongoTemplate.insert(poke,collectionNameKey+"poke_"+symbol+"_"+period+"_kline");
        }
//        mongoTemplate.insert(poke,collectionNameKey+"poke_"+symbol+"_depth");
        mongoTemplate.insert(poke,collectionNameKey+"poke_"+symbol+"_detail");
        mongoTemplate.insert(poke,collectionNameKey+"poke_"+symbol+"_trade");
    }

    @Override
    public Page<ContractSecondCoin> findAll(PageParam pageParam) {
        Page<ContractSecondCoin> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        LambdaQueryWrapper<ContractSecondCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ContractSecondCoin::getSort);
        return this.page(page,queryWrapper);
    }
}
