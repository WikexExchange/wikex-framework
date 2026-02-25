package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.mapper.CoinprotocolMapper;
import com.wikex.wikex.user.service.CoinprotocolService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CoinprotocolServiceImpl extends ServiceImpl<CoinprotocolMapper, Coinprotocol> implements CoinprotocolService {

    @Override
    public List<CoinprotocolDTO> allCoinprotocolList() {
        return this.baseMapper.allCoinprotocolList();
    }

    @Override
    public Coinprotocol findByProtocol(Integer protocol) {
        QueryWrapper<Coinprotocol> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("protocol",protocol);
        return this.getOne(queryWrapper);
    }

    @Override
    public Page<Coinprotocol> findAll(Integer pageNo, Integer pageSize) {
        Page<Coinprotocol> page = new Page<>(pageNo,pageSize);
        QueryWrapper<Coinprotocol> queryWrapper = new QueryWrapper<>();
        return this.page(page,queryWrapper);
    }
}
