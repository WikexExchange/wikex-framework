package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.TransferRecord;


public interface TransferRecordService extends IService<TransferRecord> {

    IPage<TransferRecord> findAllByMemberId(long id, int pageNo, int pageSize);
}
