package com.wikex.wikex.user.vo;

import com.wikex.wikex.annotation.Excel;
import lombok.Data;

@Data
public class RechargeExcelVO {
    @Excel(name = "User ID")
    private Long memberId;

    @Excel(name = "Email")
    private String email;

    @Excel(name = "Mobile Phone")
    private String mobilePhone;

    @Excel(name = "Recharge Coin")
    private String coinname;

    @Excel(name = "Protocol Name")
    private String protocolname;

    @Excel(name = "Deposit Address")
    private String address;

    @Excel(name = "Recharge Amount")
    private String money;

    @Excel(name = "Status")
    private String status;

    @Excel(name = "Confirmations")
    private String confirms;

    @Excel(name = "Arrival Time")
    private String addtime;
}
