package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.user.entity.TransferAddress;
import com.wikex.wikex.user.mapper.TransferAddressMapper;
import com.wikex.wikex.user.service.TransferAddressService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TransferAddressServiceImpl extends ServiceImpl<TransferAddressMapper, TransferAddress> implements TransferAddressService {

    @Override
    public TransferAddress findByCoinIdAndAddress(String coinId, String address) {
        QueryWrapper<TransferAddress> query = new QueryWrapper<>();
        query.eq("coin_id",coinId).eq("address",address);
        return this.getOne(query);
    }

    @Override
    public List<TransferAddress> findByCoin(String coinId) {
        QueryWrapper<TransferAddress> query = new QueryWrapper<>();
        query.eq("coin_id",coinId).eq("status", CommonStatus.NORMAL.getCode());
        return this.list(query);
    }
}
