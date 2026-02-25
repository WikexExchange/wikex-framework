package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.WithdrawCodeRecord;


public interface WithdrawCodeRecordService extends IService<WithdrawCodeRecord> {

    WithdrawCodeRecord findByWithdrawCode(String withdrawCode);

    void withdrawSuccess(Long recodeId, Long memberId);

    IPage<WithdrawCodeRecord> findAllByMemberId( Long memberId, int pageNo, int pageSize);
}
