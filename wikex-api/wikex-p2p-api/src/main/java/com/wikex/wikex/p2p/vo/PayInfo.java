package com.wikex.wikex.p2p.vo;

import com.wikex.wikex.p2p.entity.Alipay;
import com.wikex.wikex.p2p.entity.BankInfo;
import com.wikex.wikex.p2p.entity.WechatPay;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class PayInfo {
    private String realName;
    private Alipay alipay;
    private WechatPay wechatPay;
    private BankInfo bankInfo;
}
