package com.wikex.wikex.swap.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractPosition;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.mapper.MemberContractWalletMapper;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractMarketService;
import com.wikex.wikex.swap.service.MemberContractPositionService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MemberContractWalletServiceImpl extends ServiceImpl<MemberContractWalletMapper, MemberContractWallet> implements MemberContractWalletService {

    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private MemberContractPositionService memberContractPositionService;
    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory; 

    @Autowired
    private ContractMarketService marketService;


    @Override
    public MemberContractWallet findByMemberId(Long memberId) {
        QueryWrapper<MemberContractWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void modifyUsdtBuyLeverage(Long walletId, BigDecimal leverage) {
        baseMapper.modifyUsdtBuyLeverage(walletId,leverage);
    }

    @Override
    public void modifyUsdtSellLeverage(Long walletId, BigDecimal leverage) {
        baseMapper.modifyUsdtSellLeverage(walletId,leverage);
    }

    @Override
    public void decreaseUsdtFrozen(Long walletId, BigDecimal amount) {
        int ret = baseMapper.decreaseUsdtFrozen(walletId,amount);
        if (ret == 0) {
            
            
            MemberContractWallet wallet = this.getById(walletId);
            BigDecimal subtract = amount.subtract(wallet.getUsdtFrozenBalance());
            if(subtract.compareTo(BigDecimal.valueOf(0.1))<0){
                
                baseMapper.decreaseUsdtFrozen(walletId,wallet.getUsdtFrozenBalance());
            }else {
                
            }
        }
    }

    @Override
    public MessageResult freezeUsdtBalance(MemberContractWallet memberWallet, BigDecimal amount) {
        int ret = baseMapper.freezeUsdtBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @Override
    public void increaseUsdtBuyPrincipalAmountWithFrozen(Long walletId, BigDecimal amount) {
        baseMapper.increaseUsdtBuyPrincipalAmountWithFrozen(walletId, amount);
    }

    @Override
    public void increaseUsdtSellPrincipalAmountWithFrozen(Long walletId, BigDecimal amount) {
        baseMapper.increaseUsdtSellPrincipalAmountWithFrozen(walletId, amount);
    }

    @Override
    public void updateUsdtBuyPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume) {
        baseMapper.updateUsdtBuyPriceAndPosition(walletId, avaPrice, volume);
    }

    @Override
    public void updateUsdtSellPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume) {
        baseMapper.updateUsdtSellPriceAndPosition(walletId, avaPrice, volume);
    }

    @Override
    public void updateShareNumber(Long walletId, BigDecimal shareNumber) {
        baseMapper.updateShareNumber(walletId, shareNumber);
    }

    @Override
    public void decreaseUsdtFrozenSellPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount) {
        baseMapper.decreaseUsdtFrozenSellPositionAndPrincipalAmount(walletId, volume, principalAmount);
    }

    @Override
    public void increaseUsdtBalance(Long walletId, BigDecimal amount) {
        baseMapper.increaseUsdtBalance(walletId,amount);
    }

    @Override
    public void increaseUsdtProfit(Long  walletId, BigDecimal pL) {
        baseMapper.increaseUsdtProfit(walletId, pL);
    }

    @Override
    public void increaseUsdtLoss(Long walletId, BigDecimal pL) {
        baseMapper.increaseUsdtLoss(walletId, pL);
    }

    @Override
    public void decreaseUsdtFrozenBuyPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount) {
        baseMapper.decreaseUsdtFrozenBuyPositionAndPrincipalAmount(walletId, volume, principalAmount);
    }

    @Override
    public void freezeUsdtSellPosition(Long walletId, BigDecimal volume) {
        baseMapper.freezeUsdtSellPosition(walletId, volume);
    }

    @Override
    public void freezeUsdtBuyPosition(Long walletId, BigDecimal volume) {
        baseMapper.freezeUsdtBuyPosition(walletId, volume);
    }

    @Override
    public List<MemberContractWallet> findAllNeedSync(ContractCoin contractCoin) {
        return baseMapper.findAllNeedSync(contractCoin.getId());
    }

    @Override
    public void blastBuy(Long walletId) {
        baseMapper.blastBuy(walletId);
    }

    @Override
    public void blastSell(Long walletId) {
        baseMapper.blastSell(walletId);
    }

    @Override
    public void decreaseUsdtBalance(Long walletId, BigDecimal amount) {
        baseMapper.decreaseUsdtBalance(walletId, amount);
    }

    @Override
    public MessageResult thawUsdtBalance(MemberContractWallet memberWallet, BigDecimal amount) {
        int ret = baseMapper.thawUsdtBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @Override
    public void thrawUsdtSellPosition(Long walletId, BigDecimal volume) {
        baseMapper.thrawUsdtSellPosition(walletId, volume);
    }

    @Override
    public void thrawUsdtBuyPosition(Long walletId, BigDecimal volume) {
        baseMapper.thrawUsdtBuyPosition(walletId, volume);
    }

    @Override
    public void increaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.increaseUsdtBuyPrincipalAmount(walletId, amount);
    }

    @Override
    public void modifyUsdtBuyAndSellLeverage(Long walletId, BigDecimal leverage) {
        baseMapper.modifyUsdtBuyAndSellLeverage(walletId, leverage);
    }

    @Override
    public void increaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.increaseUsdtSellPrincipalAmount(walletId, amount);
    }
    
    @Override
    public void decreaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.decreaseUsdtBuyPrincipalAmount(walletId, amount);
    }

    @Override
    public void decreaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.decreaseUsdtSellPrincipalAmount(walletId, amount);
    }

    @Override
    public List<MemberContractWallet> findAllByMemberId(Long id) {
        QueryWrapper<MemberContractWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",id);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Page<MemberContractWallet> findAll(MemberContractWalletScreen screen) {
        Page<MemberContractWallet> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        QueryWrapper<MemberContractWallet> queryWrapper = new QueryWrapper<>();
        if (screen.getContractId() != null) {
            queryWrapper.eq("contract_id",screen.getContractId());
        }
        if(screen.getMemberId() != null) {
            queryWrapper.eq("member_id",screen.getMemberId());
        }
        if(StringUtils.isNotEmpty(screen.getPhone())) {
            Member member = memberFeign.findByPhone(screen.getPhone());
            queryWrapper.eq("member_id",member.getId());
        }
        if(StringUtils.isNotEmpty(screen.getEmail())) {
            Member member = memberFeign.findByEmail(screen.getEmail());
            queryWrapper.eq("member_id",member.getId());
        }
        if(screen.getUsdtBalance() != null) {
            queryWrapper.ge("usdt_balance",screen.getUsdtBalance());
        }
        if(screen.getUsdtFrozenBalance() != null) {
            queryWrapper.ge("usdt_frozen_balance",screen.getUsdtFrozenBalance());
        }
        if(screen.getUsdtPattern() != null) {
            queryWrapper.eq("usdt_pattern",screen.getUsdtPattern().getCode());
        }
        if(screen.getUsdtBuyLeverage() != null) {
            queryWrapper.ge("usdt_buy_leverage",screen.getUsdtBuyLeverage());
        }
        if(screen.getUsdtSellLeverage() != null) {
            queryWrapper.ge("usdt_sell_leverage",screen.getUsdtSellLeverage());
        }
        if(screen.getUsdtBuyPosition() != null) {
            queryWrapper.ge("usdt_buy_position",screen.getUsdtBuyPosition());
        }
        if(screen.getUsdtFrozenBuyPosition() != null) {
            queryWrapper.ge("usdt_frozen_buy_position",screen.getUsdtFrozenBuyPosition());
        }
        if(screen.getUsdtBuyPrincipalAmount() != null) {
            queryWrapper.ge("usdt_buy_principal_amount",screen.getUsdtBuyPrincipalAmount());
        }
        if(screen.getUsdtSellPosition() != null) {
            queryWrapper.ge("usdt_sell_position",screen.getUsdtSellPosition());
        }
        if(screen.getUsdtFrozenSellPosition() != null) {
            queryWrapper.ge("usdt_frozen_sell_position",screen.getUsdtFrozenSellPosition());
        }
        if(screen.getUsdtSellPrincipalAmount() != null) {
            queryWrapper.ge("usdt_sell_principal_amount",screen.getUsdtSellPrincipalAmount());
        }
        queryWrapper.orderByDesc("usdt_balance");
        Page<MemberContractWallet> walletPage = this.page(page,queryWrapper);
        List<ContractCoin> list = contractCoinService.list();
        Map<Long, ContractCoin> coinMap = list.stream().collect(
                Collectors.toMap(x -> x.getId(), x->x));





        return walletPage;


    }

    public List<MemberContractWallet> getWalletsByBuyPosition() {
        return baseMapper.getWalletsByBuyPosition();
    }

    public List<MemberContractWallet> getWalletsBySellPosition() {
        return baseMapper.getWalletsBySellPosition();
    }

    
    public void justDecreaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justDecreaseUsdtBuyPrincipalAmount(walletId, amount);
    }

    
    public void justIncreaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justIncreaseUsdtSellPrincipalAmount(walletId,amount);
    }

    
    public void justDecreaseUsdtSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justDecreaseUsdtSellPrincipalAmount(walletId,amount);
    }

    
    public void justIncreaseUsdtBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justIncreaseUsdtBuyPrincipalAmount(walletId,amount);
    }

    @Override
    public void decreaseUsdtBuyPositionAndUsdtFrozenBuyPositionAndPrincipalAmount(Long id, BigDecimal position, BigDecimal frozenPosition, BigDecimal principalAmount) {
        baseMapper.decreaseUsdtBuyPositionAndUsdtFrozenBuyPositionAndPrincipalAmount(id,position,frozenPosition,principalAmount);
    }

    @Override
    public void decreaseUsdtSellPositionAndUsdtFrozenSellPositionAndPrincipalAmount(Long id, BigDecimal position, BigDecimal frozenPosition, BigDecimal principalAmount) {
        baseMapper.decreaseUsdtSellPositionAndUsdtFrozenSellPositionAndPrincipalAmount(id,position,frozenPosition,principalAmount);
    }

    @Override
    public List<MemberContractWallet> holdingWalletList(Long contractId) {
        return baseMapper.holdingWalletList(contractId);
    }

    @Override
    public BigDecimal usdtTotalProfitAndLoss(Long memberId,List<ContractCoin> coins) {
        BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;
        
        for (ContractCoin coin : coins) {
            
            List<MemberContractPosition> positions = memberContractPositionService.queryHoldingPositions(memberId,coin.getId());
            ContractCoinMatch contractCoinMatch = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            if(contractCoinMatch==null){
                
                continue;
            }
            BigDecimal currentPrice = contractCoinMatch.getNowPrice();
            if(positions!=null && positions.size()>0) {
                for (MemberContractPosition position : positions) {
                    if (position.getDirection().equals(ContractOrderDirection.BUY)) {
                        usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(currentPrice.subtract(position.getPrice()).multiply(
                                position.getPrincipalAmount().multiply(position.getLeverage()).divide(position.getPrice(),8,BigDecimal.ROUND_HALF_DOWN)
                        ));
                    }else {
                        usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(position.getPrice().subtract(currentPrice).multiply(
                                position.getPrincipalAmount().multiply(position.getLeverage()).divide(position.getPrice(),8,BigDecimal.ROUND_HALF_DOWN)
                        ));
                    }
                }
            }
        }

        return usdtTotalProfitAndLoss;

    }

    public static void main(String[] args) {
        BigDecimal currentPrice = BigDecimal.valueOf(0.9448);
        BigDecimal price = BigDecimal.valueOf(0.9786);
        BigDecimal principalAmount = BigDecimal.valueOf(500);
        BigDecimal sn = BigDecimal.valueOf(100);
        BigDecimal multiply = currentPrice.subtract(price).multiply(
                principalAmount.multiply(sn).divide(price, 8, BigDecimal.ROUND_HALF_DOWN)
        );
        System.out.println(multiply.toPlainString());
    }

    @Override
    public Map<String,BigDecimal> getTotalProfitAndLossAndPrincipalAmount(Long memberId,List<ContractCoin> coins,Boolean isForBlast,Map<String,List<Poke>> pokesMap) {
        Map<String,BigDecimal> result = new HashMap<>();
        BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;
        BigDecimal principalAmount = BigDecimal.ZERO;
        BigDecimal buyPrincipalAmount = BigDecimal.ZERO;
        BigDecimal sellPrincipalAmount = BigDecimal.ZERO;
        boolean isHave = false;
        
        for (ContractCoin coin : coins) {
            
            List<MemberContractPosition> positions = memberContractPositionService.queryHoldingPositions(memberId,coin.getId());
            ContractCoinMatch contractCoinMatch = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            if(contractCoinMatch==null){
                
                continue;
            }
            BigDecimal currentPrice = contractCoinMatch.getNowPrice();
            if (isForBlast) {
                
                List<Poke> pokes =pokesMap!=null ? pokesMap.get(coin.getSymbol()) : null;
                if (pokes != null && pokes.size() > 0) {
                    currentPrice = new BigDecimal(pokes.get(0).getPrice());
                    
                    isHave = true;
                }
            }
            result.put(coin.getSymbol(),currentPrice);
            if(positions!=null && positions.size()>0) {
                for (MemberContractPosition position : positions) {
                    
                    principalAmount = principalAmount.add(position.getPrincipalAmount());
                    if (position.getDirection().equals(ContractOrderDirection.BUY)) {
                        buyPrincipalAmount = buyPrincipalAmount.add(position.getPrincipalAmount());
                        usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(currentPrice.subtract(position.getPrice()).multiply(
                                position.getPrincipalAmount().multiply(position.getLeverage()).divide(position.getPrice(),8,BigDecimal.ROUND_HALF_DOWN)
                        ));
                        
                    }else {
                        sellPrincipalAmount = sellPrincipalAmount.add(position.getPrincipalAmount());
                        usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(position.getPrice().subtract(currentPrice).multiply(
                                position.getPrincipalAmount().multiply(position.getLeverage()).divide(position.getPrice(),8,BigDecimal.ROUND_HALF_DOWN)
                        ));
                        
                    }
                }
            }
        }
        result.put("profitAndLoss",usdtTotalProfitAndLoss);
        result.put("principalAmount",principalAmount);
        result.put("buyPrincipalAmount",buyPrincipalAmount);
        result.put("sellPrincipalAmount",sellPrincipalAmount);
        if(isHave){
            
        }
        return result;

    }

    @Override
    public List<MemberContractWallet> findByNotInMemberIds(List<Long> memberIds) {
        QueryWrapper<MemberContractWallet> queryWrapper = new QueryWrapper<>();
        if(memberIds!=null && memberIds.size()>0) {
            queryWrapper.notIn("member_id", memberIds);
        }
        return this.list(queryWrapper);
    }
}
