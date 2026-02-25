package com.wikex.wikex.option.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOptionStatus;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.mapper.ContractOptionMapper;
import com.wikex.wikex.option.service.ContractOptionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.pojo.PresetPrice;
import com.wikex.wikex.screen.ContractOptionScreen;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class ContractOptionServiceImpl extends ServiceImpl<ContractOptionMapper, ContractOption> implements ContractOptionService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ContractOption> findBySymbolAndStatus(String symbol, ContractOptionStatus status) {
        LambdaQueryWrapper<ContractOption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOption::getSymbol,symbol);
        queryWrapper.eq(ContractOption::getStatus,status.getCode());
        return this.list(queryWrapper);
    }

    @Override
    public ContractOption findBySymbolAndOptionNo(String symbol, int perOptionNo) {
        LambdaQueryWrapper<ContractOption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOption::getSymbol,symbol);
        queryWrapper.eq(ContractOption::getOptionNo,perOptionNo);
        List<ContractOption> list = this.list(queryWrapper);
        if(list!=null && list.size()>0){
            return list.get(0);
        }else {
            return null;
        }
    }

    @Override
    public void savePresetPrice(String symbol, BigDecimal price) {
        PresetPrice presetPrice = new PresetPrice(price.stripTrailingZeros().toPlainString());
//        mongoTemplate.insert(presetPrice,"contract_preset_price_"+symbol+"_kline");
//        mongoTemplate.insert(presetPrice,"contract_preset_price_"+symbol+"_depth");
        mongoTemplate.insert(presetPrice,"contract_preset_price_"+symbol+"_detail");
//        mongoTemplate.insert(presetPrice,"contract_preset_price_"+symbol+"_trade");
        Poke poke = new Poke(price.stripTrailingZeros().toPlainString());
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_kline");
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_depth");
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_detail");
        mongoTemplate.insert(poke,"contract_poke_"+symbol+"_trade");
    }

    @Override
    public Page<ContractOption> findAll(String symbol, int count) {
        Page<ContractOption> page = new Page<>(1,count);
        LambdaQueryWrapper<ContractOption> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(symbol)){
            queryWrapper.eq(ContractOption::getSymbol,symbol);
        }
        queryWrapper.eq(ContractOption::getStatus,ContractOptionStatus.CLOSED.getCode());
        queryWrapper.orderByDesc(ContractOption::getCreateTime);

        return this.page(page,queryWrapper);
    }

    @Override
    public ContractOption findOne(Long optionId) {
        return this.getById(optionId);
    }

    @Override
    public Page<ContractOption> findAll(ContractOptionScreen screen) {
        Page<ContractOption> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        LambdaQueryWrapper<ContractOption> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(screen.getSymbol())) {
            queryWrapper.eq(ContractOption::getSymbol,screen.getSymbol());
        }
        if (screen.getOptionNo() != null) {
            queryWrapper.eq(ContractOption::getOptionNo,screen.getOptionNo());
        }
        if (screen.getTotalBuyCount() != null) {
            queryWrapper.gt(ContractOption::getTotalBuyCount,screen.getTotalBuyCount());
        }
        if (screen.getTotalSellCount() != null) {
            queryWrapper.gt(ContractOption::getTotalSellCount,screen.getTotalSellCount());
        }
        if (screen.getTotalPl() != null) {
            queryWrapper.gt(ContractOption::getTotalPl,screen.getTotalPl());
        }
        queryWrapper.orderByDesc(ContractOption::getCreateTime);
        return this.page(page,queryWrapper);
    }
}
