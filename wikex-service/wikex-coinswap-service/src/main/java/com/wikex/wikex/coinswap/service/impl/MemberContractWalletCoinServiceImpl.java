package com.wikex.wikex.coinswap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.MemberContractWalletCoinScreen;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.mapper.MemberContractWalletCoinMapper;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class MemberContractWalletCoinServiceImpl extends ServiceImpl<MemberContractWalletCoinMapper, MemberContractWalletCoin> implements MemberContractWalletCoinService {

    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinCoinService contractCoinService;

    @Override
    public MemberContractWalletCoin findByMemberIdAndContractCoin(Long memberId, ContractCoinCoin contractCoin) {
        QueryWrapper<MemberContractWalletCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId).eq("contract_id",contractCoin.getId());
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void modifyCoinBuyLeverage(Long walletId, BigDecimal leverage) {
        baseMapper.modifyCoinBuyLeverage(walletId,leverage);
    }

    @Override
    public void modifyCoinSellLeverage(Long walletId, BigDecimal leverage) {
        baseMapper.modifyCoinSellLeverage(walletId,leverage);
    }

    @Override
    public void decreaseCoinFrozen(Long walletId, BigDecimal amount) {
        baseMapper.decreaseCoinFrozen(walletId,amount);
    }

    @Override
    public MessageResult freezeCoinBalance(MemberContractWalletCoin memberWallet, BigDecimal amount) {
        int ret = baseMapper.freezeCoinBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @Override
    public void increaseCoinBuyPrincipalAmountWithFrozen(Long walletId, BigDecimal amount) {
        baseMapper.increaseCoinBuyPrincipalAmountWithFrozen(walletId, amount);
    }

    @Override
    public void increaseCoinSellPrincipalAmountWithFrozen(Long walletId, BigDecimal amount) {
        baseMapper.increaseCoinSellPrincipalAmountWithFrozen(walletId, amount);
    }

    @Override
    public void updateCoinBuyPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume) {
        baseMapper.updateCoinBuyPriceAndPosition(walletId, avaPrice, volume);
    }

    @Override
    public void updateCoinSellPriceAndPosition(Long walletId, BigDecimal avaPrice, BigDecimal volume) {
        baseMapper.updateCoinSellPriceAndPosition(walletId, avaPrice, volume);
    }

    @Override
    public void updateShareNumber(Long walletId, BigDecimal shareNumber) {
        baseMapper.updateShareNumber(walletId, shareNumber);
    }

    @Override
    public void decreaseCoinFrozenSellPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount) {
        baseMapper.decreaseCoinFrozenSellPositionAndPrincipalAmount(walletId, volume, principalAmount);
    }

    @Override
    public void increaseCoinBalance(Long walletId, BigDecimal amount) {
        baseMapper.increaseCoinBalance(walletId,amount);
    }

    @Override
    public void increaseCoinProfit(Long  walletId, BigDecimal pL) {
        baseMapper.increaseCoinProfit(walletId, pL);
    }

    @Override
    public void increaseCoinLoss(Long walletId, BigDecimal pL) {
        baseMapper.increaseCoinLoss(walletId, pL);
    }

    @Override
    public void decreaseCoinFrozenBuyPositionAndPrincipalAmount(Long walletId, BigDecimal volume, BigDecimal principalAmount) {
        baseMapper.decreaseCoinFrozenBuyPositionAndPrincipalAmount(walletId, volume, principalAmount);
    }

    @Override
    public void freezeCoinSellPosition(Long walletId, BigDecimal volume) {
        baseMapper.freezeCoinSellPosition(walletId, volume);
    }

    @Override
    public void freezeCoinBuyPosition(Long walletId, BigDecimal volume) {
        baseMapper.freezeCoinBuyPosition(walletId, volume);
    }

    @Override
    public List<MemberContractWalletCoin> findAllNeedSync(ContractCoinCoin contractCoin) {
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
    public void decreaseCoinBalance(Long walletId, BigDecimal amount) {
        baseMapper.decreaseCoinBalance(walletId, amount);
    }

    @Override
    public MessageResult thawCoinBalance(MemberContractWalletCoin memberWallet, BigDecimal amount) {
        int ret = baseMapper.thawCoinBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @Override
    public void thrawCoinSellPosition(Long walletId, BigDecimal volume) {
        baseMapper.thrawCoinSellPosition(walletId, volume);
    }

    @Override
    public void thrawCoinBuyPosition(Long walletId, BigDecimal volume) {
        baseMapper.thrawCoinBuyPosition(walletId, volume);
    }

    @Override
    public void increaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.increaseCoinBuyPrincipalAmount(walletId, amount);
    }

    @Override
    public void modifyCoinBuyAndSellLeverage(Long walletId, BigDecimal leverage) {
        baseMapper.modifyCoinBuyAndSellLeverage(walletId, leverage);
    }

    @Override
    public void increaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.increaseCoinSellPrincipalAmount(walletId, amount);
    }
    
    @Override
    public void decreaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.decreaseCoinBuyPrincipalAmount(walletId, amount);
    }

    @Override
    public void decreaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.decreaseCoinSellPrincipalAmount(walletId, amount);
    }

    @Override
    public List<MemberContractWalletCoin> findAllByMemberId(Long id) {
        QueryWrapper<MemberContractWalletCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",id);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Page<MemberContractWalletCoin> findAll(MemberContractWalletCoinScreen screen) {
        Page<MemberContractWalletCoin> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        QueryWrapper<MemberContractWalletCoin> queryWrapper = new QueryWrapper<>();
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
        if(screen.getCoinBalance() != null) {
            queryWrapper.ge("usdt_balance",screen.getCoinBalance());
        }
        if(screen.getCoinFrozenBalance() != null) {
            queryWrapper.ge("usdt_frozen_balance",screen.getCoinFrozenBalance());
        }
        if(screen.getCoinPattern() != null) {
            queryWrapper.eq("usdt_pattern",screen.getCoinPattern().getCode());
        }
        if(screen.getCoinBuyLeverage() != null) {
            queryWrapper.ge("usdt_buy_leverage",screen.getCoinBuyLeverage());
        }
        if(screen.getCoinSellLeverage() != null) {
            queryWrapper.ge("usdt_sell_leverage",screen.getCoinSellLeverage());
        }
        if(screen.getCoinBuyPosition() != null) {
            queryWrapper.ge("usdt_buy_position",screen.getCoinBuyPosition());
        }
        if(screen.getCoinFrozenBuyPosition() != null) {
            queryWrapper.ge("usdt_frozen_buy_position",screen.getCoinFrozenBuyPosition());
        }
        if(screen.getCoinBuyPrincipalAmount() != null) {
            queryWrapper.ge("usdt_buy_principal_amount",screen.getCoinBuyPrincipalAmount());
        }
        if(screen.getCoinSellPosition() != null) {
            queryWrapper.ge("usdt_sell_position",screen.getCoinSellPosition());
        }
        if(screen.getCoinFrozenSellPosition() != null) {
            queryWrapper.ge("usdt_frozen_sell_position",screen.getCoinFrozenSellPosition());
        }
        if(screen.getCoinSellPrincipalAmount() != null) {
            queryWrapper.ge("usdt_sell_principal_amount",screen.getCoinSellPrincipalAmount());
        }
        queryWrapper.orderByDesc("usdt_balance");
        Page<MemberContractWalletCoin> walletPage = this.page(page,queryWrapper);
        List<ContractCoinCoin> list = contractCoinService.list();
        Map<Long, ContractCoinCoin> coinMap = list.stream().collect(
                Collectors.toMap(x -> x.getId(), x->x));

        for (MemberContractWalletCoin record : walletPage.getRecords()) {
            record.setSymbol(coinMap.get(record.getContractId()).getSymbol());
        }

        return walletPage;


    }

    public List<MemberContractWalletCoin> getWalletsByBuyPosition(Long contractId) {
        return baseMapper.getWalletsByBuyPosition(contractId);
    }

    public List<MemberContractWalletCoin> getWalletsBySellPosition(Long contractId) {
        return baseMapper.getWalletsBySellPosition(contractId);
    }

    
    public void justDecreaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justDecreaseCoinBuyPrincipalAmount(walletId, amount);
    }

    
    public void justIncreaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justIncreaseCoinSellPrincipalAmount(walletId,amount);
    }

    
    public void justDecreaseCoinSellPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justDecreaseCoinSellPrincipalAmount(walletId,amount);
    }

    
    public void justIncreaseCoinBuyPrincipalAmount(Long walletId, BigDecimal amount) {
        baseMapper.justIncreaseCoinBuyPrincipalAmount(walletId,amount);
    }
}
