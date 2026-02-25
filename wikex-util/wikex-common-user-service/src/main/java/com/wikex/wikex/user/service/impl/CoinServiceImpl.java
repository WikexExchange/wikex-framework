package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.dto.CoinDTO;
import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.mapper.CoinMapper;
import com.wikex.wikex.user.service.CoinService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wikex.wikex.constant.BooleanEnum.IS_TRUE;


@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService {

    @Override
    public Coin findByUnit(String coinUnit) {
        QueryWrapper<Coin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("unit",coinUnit);
        return this.getOne(queryWrapper);
    }

    @Override
    public Coin findByName(String name) {
        QueryWrapper<Coin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",name);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<Coin> findLegalAll() {
        QueryWrapper<Coin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("has_legal",true);
        return this.list(queryWrapper);
    }

    @Override
    public IPage findLegalCoinPage(Integer pageNo, Integer pageSize) {
        IPage<Coin> page = new Page<>(pageNo,pageSize);
        QueryWrapper<Coin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("has_legal",true);
        queryWrapper.orderByAsc("sort");
        return this.page(page,queryWrapper);

    }

    @Override
    public Page<Coin> findAll(Integer pageNo, Integer pageSize) {
        Page<Coin> page = new Page<>(pageNo,pageSize);
        QueryWrapper<Coin> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("name");
        return this.page(page,queryWrapper);
    }

    @Override
    public List<CoinDTO> findAllNameAndUnit() {
        return this.baseMapper.findAllNameAndUnit();
    }

    @Override
    public List<Coin> findAllCanWithDraw() {
        QueryWrapper<Coin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("has_legal",false);
        queryWrapper.eq("status", CommonStatus.NORMAL.getCode());
        queryWrapper.eq("can_withdraw",IS_TRUE.getCode());
        queryWrapper.orderByAsc("sort");
        return this.list(queryWrapper);
    }

    @Override
    public List<ContractDTO> getContractByProtocol(String protocol) {
        return this.baseMapper.getContractByProtocol(protocol);
    }

    @Override
    public List<String> getAllCoinName() {
        List<String> list = this.baseMapper.findAllName();
        return list;
    }

    @Override
    public Long getMaxId() {
        Long maxId = this.baseMapper.getMaxId();
        return maxId==null?0:maxId;
    }
}
