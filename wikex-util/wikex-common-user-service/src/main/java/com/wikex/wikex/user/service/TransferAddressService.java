package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.TransferAddress;

import java.util.List;


public interface TransferAddressService extends IService<TransferAddress> {

    TransferAddress findByCoinIdAndAddress(String coinId, String address);

    List<TransferAddress> findByCoin(String coinId);
}
