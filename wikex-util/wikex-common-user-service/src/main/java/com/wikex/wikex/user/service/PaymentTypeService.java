package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.PaymentType;
import com.wikex.wikex.user.vo.PaymentTypeConfig;

import java.util.List;

/**
 * @author zhujunjun
 */
public interface PaymentTypeService extends IService<PaymentType> {


    List<PaymentType> findAll();

    List<PaymentTypeConfig> findPaymentTypeConfigById(Long id);

    PaymentType findPaymentTypeById(Long id);
}
