package com.wikex.wikex.user.feign;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.PaymentType;
import com.wikex.wikex.user.entity.PaymentTypeRecord;
import com.wikex.wikex.user.service.PaymentTypeRecordService;
import com.wikex.wikex.user.service.PaymentTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("paymentFeign")
public class PaymentFeignController extends BaseController {
    @Autowired
    private PaymentTypeService paymentTypeService;
    @Autowired
    private PaymentTypeRecordService paymentTypeRecordService;

    @RequestMapping("findPaymentTypeById")
    public PaymentType findPaymentTypeById(@RequestParam("id") Long id) {
        return paymentTypeService.findPaymentTypeById(id);
    }

    @RequestMapping("getRecordsByUserId")
    public List<PaymentTypeRecord> getRecordsByUserId(@RequestParam("memberId") Long memberId) {
        List<PaymentTypeRecord> records = paymentTypeRecordService.getRecordsByUserId(memberId);
        return records;
    }

}
