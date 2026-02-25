package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberDepositScreen;
import com.wikex.wikex.user.entity.MemberDeposit;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemberDepositMapper extends BaseMapper<MemberDeposit> {
        Page<MemberDeposit> findAll(@Param("screen") MemberDepositScreen screen);

        List<MemberDeposit> getDepositStatistics(@Param("dateStr") String dateStr);

        MemberDeposit findDeposit(@Param("address") String address, @Param("txHash") String txHash,
                        @Param("logIndex") Integer logIndex);

        MemberDeposit findDepositForUpdate(@Param("address") String address, @Param("txHash") String txHash,
                        @Param("logIndex") Integer logIndex);

        Page<MemberDeposit> findByMemberId(Page<MemberDeposit> page, @Param("memberId") Integer memberId);
}
