package com.wikex.wikex.second.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.screen.ContractSecondOrderScreen;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.wikex.wikex.second.mapper.ContractSecondOrderMapper;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import com.wikex.wikex.second.service.ContractSecondOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.MemberSecondWallet;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.feign.MemberSecondWalletFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author markchao
 * @since 2022-03-27
 */
@Service
public class ContractSecondOrderServiceImpl extends ServiceImpl<ContractSecondOrderMapper, ContractSecondOrder> implements ContractSecondOrderService {

    @Autowired
    private ContractSecondCoinService contractSecondCoinService;
    @Autowired
    private MemberSecondWalletFeign memberSecondWalletService;
    @Autowired
    private MemberTransactionFeign memberTransactionService;

    @Override
    public List<ContractSecondOrder> findBySymbolAndStatusAndCloseTime(String symbol, ContractSecondOrderStatus open, Date closeTime) {
        LambdaQueryWrapper<ContractSecondOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractSecondOrder::getSymbol,symbol);
        queryWrapper.eq(ContractSecondOrder::getStatus,open.getCode());
        queryWrapper.le(ContractSecondOrder::getCloseTime,closeTime);
        return this.list(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void closeOrder(ContractSecondOrder order, BigDecimal closePrice) {
        ContractSecondOrderResult result = ContractSecondOrderResult.LOSE;
        if(order.getPreClosePrice()!=null && order.getPreClosePrice().compareTo(BigDecimal.ZERO)==1){
            closePrice = order.getPreClosePrice();
            
            contractSecondCoinService.savePoke(order.getSymbol(),closePrice);
        }
        if(closePrice.compareTo(order.getOpenPrice())==1 && order.getDirection().equals(ContractSecondOrderDirection.BUY)){
            result = ContractSecondOrderResult.WIN;
        }else if(closePrice.compareTo(order.getOpenPrice())==0){
            result = ContractSecondOrderResult.TIED;
        }else if(closePrice.compareTo(order.getOpenPrice())==-1 && order.getDirection().equals(ContractSecondOrderDirection.SELL)){
            result = ContractSecondOrderResult.WIN;
        }
        order.setResult(result);
        order.setClosePrice(closePrice);
        
        BigDecimal winReward = BigDecimal.ZERO;
        if(result.equals(ContractSecondOrderResult.WIN)){
            winReward = order.getBetAmount().multiply(order.getCycleRate());
        }else if(result.equals(ContractSecondOrderResult.LOSE) && order.getType().equals(ContractSecondOrderType.NO)){
            winReward = BigDecimal.ZERO.subtract(order.getBetAmount());
        }
        order.setWinAmount(winReward);
        order.setStatus(ContractSecondOrderStatus.CLOSE);
        order.setUpdateTime(new Date());
        this.saveOrUpdate(order);
        
        MemberSecondWallet wallet = memberSecondWalletService.findByCoinUnitAndMemberId(order.getCoinSymbol(), order.getMemberId());
        
        if(result.equals(ContractSecondOrderResult.WIN) || result.equals(ContractSecondOrderResult.TIED) || result.equals(ContractSecondOrderResult.CANCELED) || order.getType().equals(ContractSecondOrderType.YES)){
            
            memberSecondWalletService.thawBalance(order.getCoinSymbol(), order.getMemberId(), order.getBetAmount()); 
        }else {
            
            memberSecondWalletService.decreaseFrozen(wallet.getId(), order.getBetAmount());
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(BigDecimal.ZERO.subtract(order.getBetAmount()));
            memberTransaction.setSymbol(order.getCoinSymbol());
            memberTransaction.setType(TransactionType.SECOND_FAIL.getCode());
            memberTransaction.setMemberId(order.getMemberId());
            memberTransaction.setRealFee("0");
            memberTransaction.setDiscountFee("0");
            memberTransaction.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction);
        }
        

        
        if(order.getWinAmount().compareTo(BigDecimal.ZERO)==1){
            
            memberSecondWalletService.increaseBalance(wallet.getId(), order.getWinAmount());
            
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(order.getWinAmount());
            memberTransaction.setSymbol(order.getCoinSymbol());
            memberTransaction.setType(TransactionType.SECOND_REWARD.getCode());
            memberTransaction.setMemberId(order.getMemberId());
            memberTransaction.setRealFee("0");
            memberTransaction.setDiscountFee("0");
            memberTransaction.setCreateTime(new Date());
            memberTransactionService.save(memberTransaction);
        }


    }

    @Override
    public Page<ContractSecondOrder> findAll(Long memberId, String symbol, int pageNo, int pageSize) {
        Page<ContractSecondOrder> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<ContractSecondOrder> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(symbol)) {
            queryWrapper.eq(ContractSecondOrder::getSymbol,symbol);
        }
        queryWrapper.eq(ContractSecondOrder::getMemberId,memberId);

        queryWrapper.and(
                wrapper->wrapper.ne(ContractSecondOrder::getStatus,ContractSecondOrderStatus.OPEN.getCode())
                        .or().le(ContractSecondOrder::getCloseTime,new Date())
        );
        queryWrapper.orderByDesc(ContractSecondOrder::getCreateTime);

        return this.page(page,queryWrapper);
    }

    @Override
    public Page<ContractSecondOrder> findAll(ContractSecondOrderScreen screen) {

        Page<ContractSecondOrder> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        LambdaQueryWrapper<ContractSecondOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(screen.getSymbol())) {
            queryWrapper.eq(ContractSecondOrder::getSymbol,screen.getSymbol());
        }
        if (screen.getMemberId() != null) {
            queryWrapper.eq(ContractSecondOrder::getMemberId,screen.getMemberId());
        }
        queryWrapper.orderByDesc(ContractSecondOrder::getCreateTime);

        return this.page(page,queryWrapper);
    }

    @Override
    public void updatePreClosePrice(Long id, BigDecimal presetPrice) {
        LambdaUpdateWrapper<ContractSecondOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ContractSecondOrder::getPreClosePrice,presetPrice);
        updateWrapper.eq(ContractSecondOrder::getId,id);
        this.update(updateWrapper);
    }

    @Override
    public List<ContractSecondOrder> findOpeningOrders(long memberId, String symbol) {
        return this.baseMapper.findOpeningOrders(memberId,symbol,new Date());
    }

    @Override
    public List<ContractSecondOrder> findByMemberIdAndSymbolAndStatus(Long memberId, String symbol, ContractSecondOrderStatus open) {
        LambdaQueryWrapper<ContractSecondOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractSecondOrder::getMemberId,memberId);
        queryWrapper.eq(ContractSecondOrder::getSymbol,symbol);
        queryWrapper.eq(ContractSecondOrder::getStatus,open.getCode());
        return this.list(queryWrapper);
    }

    @Override
    public Integer countOrderByTime(Long memberId, Date start, Date end) {
        LambdaQueryWrapper<ContractSecondOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractSecondOrder::getMemberId,memberId);
        queryWrapper.ge(ContractSecondOrder::getCreateTime,start);
        queryWrapper.le(ContractSecondOrder::getCreateTime,end);
        return this.count(queryWrapper);
    }
}
