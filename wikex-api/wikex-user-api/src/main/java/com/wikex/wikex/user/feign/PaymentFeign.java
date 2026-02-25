package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.PaymentType;
import com.wikex.wikex.user.entity.PaymentTypeRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "paymentFeign")
public interface PaymentFeign {

    @PostMapping(value = "/paymentFeign/findPaymentTypeById")
    PaymentType findPaymentTypeById(@RequestParam("id") Long id);


    @PostMapping(value = "/paymentFeign/getRecordsByUserId")
    List<PaymentTypeRecord> getRecordsByUserId(@RequestParam("memberId") Long memberId);
}
