package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.PaymentTypeRecord;

import java.util.List;

/**
 * @author zhujunjun
 */
public interface PaymentTypeRecordService extends IService<PaymentTypeRecord> {
    List<PaymentTypeRecord> getRecordsByUserId(Long userId);

    void delRecordById(Long id);
}
