package com.wikex.wikex.coinswap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.wikex.wikex.coinswap.mapper.ContractOrderEntrustCoinMapper;
import com.wikex.wikex.coinswap.service.ContractOrderEntrustCoinService;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.constant.ContractOrderEntrustType;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.screen.ContractOrderEntrustCoinScreen;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ContractOrderEntrustCoinServiceImpl extends ServiceImpl<ContractOrderEntrustCoinMapper, ContractOrderEntrustCoin> implements ContractOrderEntrustCoinService {

    @Autowired
    private MemberFeign memberFeign;

    @Override
    public List<ContractOrderEntrustCoin> loadUnMatchOrders(Long id) {
        return baseMapper.loadUnMatchOrders(id);
    }

    @Override
    public List<ContractOrderEntrustCoin> queryAllEntrustClosingOrdersByContractCoin(Long memberId, Long contractId, ContractOrderDirection direction) {
        QueryWrapper<ContractOrderEntrustCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("contract_id",contractId);
        queryWrapper.eq("direction",direction);
        queryWrapper.eq("entrust_type", ContractOrderEntrustType.CLOSE);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_ING);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void updateStatus(Long id, ContractOrderEntrustStatus status) {
        baseMapper.updateStatus(id, status);
    }

    @Override
    public IPage<ContractOrderEntrustCoin> queryPageEntrustingOrdersBySymbol(Long memberId, Long contractCoinId, int pageNo, int pageSize) {
        IPage<ContractOrderEntrustCoin> page = new Page<>(pageNo,pageSize);
        QueryWrapper<ContractOrderEntrustCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("contract_id",contractCoinId);
        queryWrapper.eq("status",ContractOrderEntrustStatus.ENTRUST_ING);
        queryWrapper.orderByDesc("create_time");
        return page(page,queryWrapper);
    }

    @Override
    public IPage<ContractOrderEntrustCoin> queryPageEntrustHistoryOrdersBySymbol(Long memberId, Long contractCoinId, int pageNo, int pageSize) {
        IPage<ContractOrderEntrustCoin> page = new Page<>(pageNo,pageSize);
        QueryWrapper<ContractOrderEntrustCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("contract_id",contractCoinId);
        queryWrapper.ne("status",ContractOrderEntrustStatus.ENTRUST_ING);
        queryWrapper.orderByDesc("create_time");
        return page(page,queryWrapper);
    }

    @Override
    public long queryEntrustingOrdersCountByContractCoinIdAndPattern(Long memberId, Long contractCoinId, ContractOrderPattern pattern) {
        QueryWrapper<ContractOrderEntrustCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("contract_id",contractCoinId);
        queryWrapper.eq("patterns",pattern);
        queryWrapper.eq("status",ContractOrderEntrustStatus.ENTRUST_ING);
        return count(queryWrapper);
    }

    @Override
    public long queryEntrustingOrdersCountByContractCoinId(Long memberId, Long contractCoinId) {
        QueryWrapper<ContractOrderEntrustCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("contract_id",contractCoinId);
        queryWrapper.eq("status",ContractOrderEntrustStatus.ENTRUST_ING);
        return count(queryWrapper);
    }

    @Override
    public List<ContractOrderEntrustCoin> findCanRewardOrders() {
        QueryWrapper<ContractOrderEntrustCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_reward",0);
        queryWrapper.eq("status",ContractOrderEntrustStatus.ENTRUST_SUCCESS);
        return list(queryWrapper);
    }

    @Override
    public Page<ContractOrderEntrustCoin> pageQuery(ContractOrderEntrustCoinScreen screen) {
        Page<ContractOrderEntrustCoin> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        QueryWrapper<ContractOrderEntrustCoin> queryWrapper = new QueryWrapper<>();
        if (screen.getContractId() != null) {
            queryWrapper.eq("contract_id",screen.getContractId());
        }
        if(screen.getStartTime() != null) {
            queryWrapper.ge("create_time",screen.getStartTime().getTime());
        }
        if(screen.getEndTime() != null) {
            queryWrapper.le("create_time",screen.getEndTime().getTime());
        }
        if(screen.getDirection() != null) {
            queryWrapper.eq("direction",screen.getDirection().getCode());
        }
        if(screen.getEntrustType() != null) {
            queryWrapper.eq("entrust_type",screen.getEntrustType());
        }
        if(screen.getIsBlast() != null) {
            queryWrapper.eq("is_blast",screen.getIsBlast());
        }
        if(screen.getIsFromSpot() != null) {
            queryWrapper.eq("is_from_spot",screen.getIsFromSpot());
        }
        if(screen.getMemberId() != null) {
            queryWrapper.eq("member_id",screen.getMemberId());
        }
        if(screen.getStatus() != null) {
            queryWrapper.eq("status",screen.getStatus());
        }
        if(screen.getType() != null) {
            queryWrapper.eq("type",screen.getType());
        }
        if(screen.getVolume() != null) {
            queryWrapper.ge("volume",screen.getVolume());
        }
        if(StringUtils.isNotEmpty(screen.getPhone())) {
            Member member = memberFeign.findByPhone(screen.getPhone());
            queryWrapper.eq("member_id",member.getId());
        }
        if(StringUtils.isNotEmpty(screen.getEmail())) {
            Member member = memberFeign.findByEmail(screen.getEmail());
            queryWrapper.eq("member_id",member.getId());
        }
        if(screen.getProfitAndLoss() != null) {
            queryWrapper.gt("profit_and_loss",screen.getProfitAndLoss());
        }
        queryWrapper.orderByDesc("create_time");

        return this.page(page,queryWrapper);
    }
}
