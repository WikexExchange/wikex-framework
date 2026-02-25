package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.MemberRechargeAddress;

import java.util.List;


public interface MemberRechargeAddressService extends IService<MemberRechargeAddress> {

    List<MemberRechargeAddress> findMemberRechargeAddressByMemberIdAndCoin(Long memberId, String name);
}
