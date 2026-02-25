package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.MemberRechargeAddress;
import com.wikex.wikex.user.mapper.MemberRechargeAddressMapper;
import com.wikex.wikex.user.service.MemberRechargeAddressService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MemberRechargeAddressServiceImpl extends ServiceImpl<MemberRechargeAddressMapper, MemberRechargeAddress> implements MemberRechargeAddressService {

    @Override
    public List<MemberRechargeAddress> findMemberRechargeAddressByMemberIdAndCoin(Long memberId, String name) {
        return this.baseMapper.findMemberRechargeAddressByMemberIdAndCoin(memberId,name);
    }
}
