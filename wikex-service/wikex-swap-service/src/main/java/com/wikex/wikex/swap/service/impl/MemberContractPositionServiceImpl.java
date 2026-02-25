package com.wikex.wikex.swap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.swap.entity.MemberContractPosition;
import com.wikex.wikex.swap.mapper.MemberContractPositionMapper;
import com.wikex.wikex.swap.service.MemberContractPositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.util.MessageResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class MemberContractPositionServiceImpl extends ServiceImpl<MemberContractPositionMapper, MemberContractPosition> implements MemberContractPositionService {

    @Override
    public IPage<MemberContractPosition> queryPageHoldingPositions(Long memberId, Long contractCoinId, int pageNo, int pageSize) {
        IPage<MemberContractPosition> page = new Page<>(pageNo,pageSize);
        QueryWrapper<MemberContractPosition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("contract_id",contractCoinId);
        queryWrapper.gt("principal_amount", 0);
        queryWrapper.orderByDesc("id");
        return page(page,queryWrapper);
    }

    @Override
    public void setZYZS(Long memberId, Long positionId, BigDecimal minPrice, BigDecimal maxPrice) {
        UpdateWrapper<MemberContractPosition> update = new UpdateWrapper<>();
        update.set("min_price",minPrice);
        update.set("max_price",maxPrice);
        update.eq("member_id",memberId);
        update.eq("id",positionId);
        this.update(update);
    }

    @Override
    public List<MemberContractPosition> getSetZyZsList(Long contractId, BigDecimal newPrice) {
        return this.baseMapper.getSetZyZsList(contractId,newPrice);
    }

    @Override
    public List<MemberContractPosition> queryHoldingPositions(Long memberId, Long contractCoinId) {
        QueryWrapper<MemberContractPosition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("contract_id",contractCoinId);
        queryWrapper.gt("principal_amount", 0);
        return list(queryWrapper);
    }

    @Override
    public void updateForcePrice(Long memberId, Long contractCoinId, BigDecimal forcePrice) {
        UpdateWrapper<MemberContractPosition> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("force_price",forcePrice);
        updateWrapper.eq("member_id",memberId);
        updateWrapper.eq("contract_id",contractCoinId);
        updateWrapper.gt("principal_amount", 0);
        this.update(updateWrapper);
    }

    @Override
    public List<Long> queryHoldingPositionMemberIds() {
        return this.baseMapper.queryHoldingPositionMemberIds();
    }

    @Override
    public List<MemberContractPosition> queryAllHoldingPositions(Long memberId) {
//        QueryWrapper<MemberContractPosition> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("member_id",memberId);
//        queryWrapper.gt("principal_amount", 0);
        return this.baseMapper.queryAllHoldingPositions(memberId);
    }

    @Override
    public List<Long> queryMemberIdsHoldingPositions(Long contractCoinId) {
        QueryWrapper<MemberContractPosition> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT member_id");
        queryWrapper.eq("contract_id",contractCoinId);
        queryWrapper.gt("principal_amount", 0);
        List<Long> memberIdList = listObjs(queryWrapper, obj -> Long.parseLong(obj.toString()));
        return memberIdList;
    }

    @Override
    public int freezePosition(Long id, BigDecimal volume) {
        int ret = baseMapper.freezePosition(id, volume);
        return ret;
    }
}
