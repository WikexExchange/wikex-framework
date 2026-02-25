package com.wikex.wikex.p2p.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wikex.wikex.constant.DepositStatusEnum;
import com.wikex.wikex.p2p.entity.DepositRecord;
import com.wikex.wikex.p2p.mapper.DepositRecordMapper;
import com.wikex.wikex.p2p.service.DepositRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DepositRecordServiceImpl extends ServiceImpl<DepositRecordMapper, DepositRecord> implements DepositRecordService {

    @Override
    public List<DepositRecord> findDepositByMemberAndStatus(Long memberId, DepositStatusEnum status) {
        LambdaQueryWrapper<DepositRecord> query = new LambdaQueryWrapper<>();
        query.eq(DepositRecord::getMemberId,memberId);
        query.eq(DepositRecord::getStatus,status.getCode());
        return this.list(query);
    }
}
