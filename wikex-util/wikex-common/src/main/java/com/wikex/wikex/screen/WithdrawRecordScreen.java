package com.wikex.wikex.screen;

import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.WithdrawStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Coin Extension")
@Data
public class WithdrawRecordScreen {

    @ApiModelProperty(value = "Coin")
    private String unit;

    @ApiModelProperty(value = "Withdrawal Address")
    private String address;

    @ApiModelProperty(value = "Withdrawal Status")
    private WithdrawStatus status;

    @ApiModelProperty(value = "Auto Withdrawal")
    private BooleanEnum isAuto;

    @ApiModelProperty(value = "Member ID")
    private Long memberId;

    @ApiModelProperty(value = "Mobile Phone")
    private String mobilePhone;

    @ApiModelProperty(value = "Order Number")
    private String orderSn;

    @ApiModelProperty(value = "Account")
    private String account;
}
