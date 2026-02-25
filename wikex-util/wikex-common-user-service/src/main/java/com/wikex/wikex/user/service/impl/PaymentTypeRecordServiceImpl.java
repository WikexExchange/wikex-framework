package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.PaymentTypeRecord;
import com.wikex.wikex.user.mapper.PaymentTypeRecordMapper;
import com.wikex.wikex.user.service.PaymentTypeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhujunjun
 */
@Service
public class PaymentTypeRecordServiceImpl extends ServiceImpl<PaymentTypeRecordMapper, PaymentTypeRecord> implements PaymentTypeRecordService {

    @Autowired
    private PaymentTypeRecordMapper paymentTypeRecordMapper;
    @Override
    public List<PaymentTypeRecord> getRecordsByUserId(Long userId) {
        return paymentTypeRecordMapper.getRecordsByUserId(userId);
    }

    @Override
    public void delRecordById(Long id) {
        this.removeById(id);
    }
}
