package com.wikex.wikex.user.feign;


import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.WalletTransRecord;
import com.wikex.wikex.user.service.WalletTransRecordService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/walletTransRecordFeign")
public class WalletTransRecordFeignController extends BaseController {

    @Autowired
    private WalletTransRecordService walletTransRecordService;

    @PostMapping("save")
    public MessageResult save(@RequestBody WalletTransRecord walletTransRecord){
        boolean ret = walletTransRecordService.saveOrUpdate(walletTransRecord);
        if (ret) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

}

