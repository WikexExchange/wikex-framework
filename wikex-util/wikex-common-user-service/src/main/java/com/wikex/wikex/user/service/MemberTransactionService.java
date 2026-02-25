package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.screen.MemberTransactionScreen;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.vo.MemberTransaction4Front;
import com.wikex.wikex.user.vo.MemberTransactionVO;
import com.wikex.wikex.user.vo.SymbolAmountSum;
import com.wikex.wikex.user.vo.MemberSymbolAmountSum;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface MemberTransactionService extends IService<MemberTransaction> {

    IPage<MemberTransaction> queryByMember(Long memberId, int pageNo, int pageSize, TransactionType type);

    Page<MemberTransaction4Front> queryByMember(Long memberId, int pageNo, int pageSize,
            TransactionType transactionType, String startTime, String endTime, String symbol);

    boolean isOverMatchLimit(String format, double gcxMatchMaxLimit);

    Page<MemberTransactionVO> joinFind(MemberTransactionScreen screen);

    MemberTransaction findOne(Long id);

    int deleteHistory(Date startTime);

    void updateRewardRobot();

    void sendExchangeReward();

    void sendSecondReward();

    void sendOptionReward();

    List<MemberTransaction> queryAllByMember(Long memberId, TransactionType type);

    // total volume
    BigDecimal getTotalVolumeOfF1(Long inviterId);

    // total fee
    BigDecimal getTotalFeeOfF1(Long inviterId);

    List<SymbolAmountSum> sumAmountByMemberSince(Long memberId, Date startTime);

    List<MemberSymbolAmountSum> sumAmountByMembersSince(List<Long> memberIds, Date startTime);
}
