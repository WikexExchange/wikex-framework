package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.MemberRechargeAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface MemberRechargeAddressMapper extends BaseMapper<MemberRechargeAddress> {

    List<MemberRechargeAddress> findMemberRechargeAddressByMemberIdAndCoin(@Param("memberId") Long memberId, @Param("name") String name);
}
