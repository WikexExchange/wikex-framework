package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.WalletTransRecord;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "walletTransRecordFeign")
public interface WalletTransRecordFeign {


    @PostMapping("/walletTransRecordFeign/save")
    MessageResult save(@RequestBody WalletTransRecord walletTransRecord);
}
