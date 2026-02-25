package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.TransferRecord;
import com.wikex.wikex.user.mapper.TransferRecordMapper;
import com.wikex.wikex.user.service.TransferRecordService;
import org.springframework.stereotype.Service;


@Service
public class TransferRecordServiceImpl extends ServiceImpl<TransferRecordMapper, TransferRecord> implements TransferRecordService {

    @Override
    public IPage<TransferRecord> findAllByMemberId(long id, int pageNo, int pageSize) {
        IPage<TransferRecord> page = new Page<>(pageNo,pageSize);
        QueryWrapper<TransferRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",id);
        queryWrapper.orderByDesc("id");
        return this.page(page,queryWrapper);
    }
}
