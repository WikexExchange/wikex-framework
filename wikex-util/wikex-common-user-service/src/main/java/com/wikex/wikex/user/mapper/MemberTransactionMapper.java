package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberTransactionScreen;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.vo.MemberTransactionVO;
import com.wikex.wikex.user.vo.SymbolAmountSum;
import com.wikex.wikex.user.vo.MemberSymbolAmountSum;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface MemberTransactionMapper extends BaseMapper<MemberTransaction> {

    Page<MemberTransactionVO> joinFind(Page<MemberTransactionVO> page, @Param("screen") MemberTransactionScreen screen);

    void updateRewardRobot();

    List<SymbolAmountSum> sumAmountByMemberSince(@Param("memberId") Long memberId, @Param("startTime") Date startTime);

    List<MemberSymbolAmountSum> sumAmountByMembersSince(@Param("memberIds") List<Long> memberIds,
            @Param("startTime") Date startTime);
}
