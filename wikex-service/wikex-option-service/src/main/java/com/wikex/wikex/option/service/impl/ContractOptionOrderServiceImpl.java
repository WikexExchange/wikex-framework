package com.wikex.wikex.option.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.wikex.wikex.option.mapper.ContractOptionCoinMapper;
import com.wikex.wikex.option.mapper.ContractOptionMapper;
import com.wikex.wikex.option.mapper.ContractOptionOrderMapper;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.option.service.ContractOptionOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.option.service.ContractOptionService;
import com.wikex.wikex.screen.ContractOptionOrderScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


@Service
public class ContractOptionOrderServiceImpl extends ServiceImpl<ContractOptionOrderMapper, ContractOptionOrder> implements ContractOptionOrderService {

    @Autowired
    private LocaleMessageSourceService localeMessageSourceService;
    @Autowired
    private ContractOptionService contractOptionService;
    @Autowired
    private ContractOptionCoinService contractOptionCoinService;

    @Override
    public List<ContractOptionOrder> findByOptionId(Long id) {
        LambdaQueryWrapper<ContractOptionOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOptionOrder::getOptionId,id);
        return this.list(queryWrapper);
    }

    @Override
    public Page<ContractOptionOrder> findAll(long memberId, String symbol, int pageNo, int pageSize) {

        Page<ContractOptionOrder> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ContractOptionOrder> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(symbol)) {
            queryWrapper.eq(ContractOptionOrder::getSymbol,symbol);
        }
        queryWrapper.eq(ContractOptionOrder::getMemberId,memberId);
        queryWrapper.orderByDesc(ContractOptionOrder::getCreateTime);
        return this.page(page,queryWrapper);
    }

    @Override
    public List<ContractOptionOrder> findByMemberIdAndOptionId(long memberId, Long optionId) {
        LambdaQueryWrapper<ContractOptionOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOptionOrder::getMemberId,memberId);
        queryWrapper.eq(ContractOptionOrder::getOptionId,optionId);
        return this.list(queryWrapper);
    }

    @Override
    public Page<ContractOptionOrder> findAll(ContractOptionOrderScreen screen) {
        Page<ContractOptionOrder> page = new Page<>(screen.getPageNo(), screen.getPageSize());
        LambdaQueryWrapper<ContractOptionOrder> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(screen.getSymbol())) {
            queryWrapper.eq(ContractOptionOrder::getSymbol,screen.getSymbol());
        }
        if (screen.getBetAmount() != null) {
            queryWrapper.gt(ContractOptionOrder::getBetAmount,screen.getBetAmount());
        }
        if (screen.getRewardAmount() != null) {
            queryWrapper.gt(ContractOptionOrder::getRewardAmount,screen.getRewardAmount());
        }
        if (screen.getMemberId() != null) {
            queryWrapper.eq(ContractOptionOrder::getMemberId,screen.getMemberId());
        }
        queryWrapper.orderByDesc(ContractOptionOrder::getCreateTime);

        return this.page(page,queryWrapper);
    }

    @Override
    public List<ContractOptionOrder> findByMemberId(Long memberId) {
        LambdaQueryWrapper<ContractOptionOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOptionOrder::getMemberId,memberId);
        return this.list(queryWrapper);
    }

    @Override
    public MessageResult setOptionOrder(Long memberId, Integer optionNo, Short optionNoChange, Short directionChange) {
        LambdaQueryWrapper<ContractOptionOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOptionOrder::getMemberId,memberId);
        queryWrapper.eq(ContractOptionOrder::getOptionNo,optionNo);
        ContractOptionOrder order = this.getOne(queryWrapper);
        if(order == null){
            return MessageResult.error(localeMessageSourceService.getMessage("ORDER_DOES_NOT_EXIST"));
        }
        ContractOption option = contractOptionService.findOne(order.getOptionId());
        if(option == null){
            return MessageResult.error(localeMessageSourceService.getMessage("CONTRACT_PERIOD_DOES_NOT_EXIST"));
        }
        ContractOptionCoin coin = contractOptionCoinService.findBySymbol(option.getSymbol());
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long timeGap = currentTime - option.getOpenTime();
        if(timeGap/1000 > coin.getCloseTimeGap()-6){
            return MessageResult.error(localeMessageSourceService.getMessage("SETUP_FAILED"));
        }
        if(optionNoChange == 2){
            order.setOptionNo(order.getOptionNo()+1);
            ContractOption nextOptions = contractOptionService.findBySymbolAndOptionNo(order.getSymbol(), order.getOptionNo());
            if(nextOptions!=null){
                order.setOptionId(nextOptions.getId());
            }else {
                return MessageResult.error(localeMessageSourceService.getMessage("CONTRACT_PERIOD_DOES_NOT_EXIST"));
            }
        }
        if(directionChange == 2){
            if(order.getDirection().getCode() == 0){
                order.setDirection(ContractOptionOrderDirection.SELL);
            }else if(order.getDirection().getCode() == 1){
                order.setDirection(ContractOptionOrderDirection.BUY);
            }
        }
        this.updateById(order);
        return MessageResult.success();
    }
}
