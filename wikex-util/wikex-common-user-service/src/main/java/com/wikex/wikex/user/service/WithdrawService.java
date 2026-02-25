package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.WithdrawScreen;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.vo.WithdrawVO;

import java.util.List;


public interface WithdrawService extends IService<Withdraw> {

    Page<Withdraw> findAllByMemberId(Long id, int page, int pageSize);

    List<Withdraw> findAllOut(WithdrawScreen withdrawScreen);

    Page<Withdraw> findAll(WithdrawScreen withdrawScreen);

    Page<Withdraw> joinFind(WithdrawScreen screen);

    List<WithdrawVO> getWithdrawStatistics(String dateStr);

    Integer countAuditing();

    void withdrawSuccess(Long withdrawId, String txid);

    void withdrawFail(Long withdrawId);

    List<Withdraw> findWithdrawByStatus(Integer status);

    void updateWithdrawStatus(Long id, Integer status);
}
