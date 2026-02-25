package com.wikex.wikex.user.vo;

import com.wikex.wikex.annotation.Excel;
import lombok.Data;

@Data
public class WithdrawExcelVO {
    @Excel(name = "User ID")
    private Long memberId;

    @Excel(name = "Email")
    private String email;

    @Excel(name = "Mobile Phone")
    private String mobilePhone;

    @Excel(name = "Withdrawal Coin")
    private String coinname;

    @Excel(name = "Protocol Name")
    private String protocolname;

    @Excel(name = "Arrival Address")
    private String address;

    @Excel(name = "Withdrawal Amount")
    private String money;

    @Excel(name = "Fee")
    private String fee;

    @Excel(name = "Real Arrival Amount")
    private String real_money;

    @Excel(name = "Withdrawal Time")
    private String addtime;

    @Excel(name = "Review Time")
    private String processtime;

    @Excel(name = "Hash")
    private String hash;

    @Excel(name = "Status")
    private String status;

}
