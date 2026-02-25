package com.wikex.wikex.screen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Withdrawal")
@Data
public class WithdrawScreen extends PageParam {

    @ApiModelProperty("Email")
    private String email;

    @ApiModelProperty("Phone")
    private String tel;

    @ApiModelProperty("Withdrawal Address")
    private String address;

    @ApiModelProperty("Protocol")
    private Integer protocol;

    @ApiModelProperty("Coin Name")
    private String coinname;

    @ApiModelProperty("Status, -1: Rejected, 0: Pending, 1: Processing, 2: Completed, 3: Failed")
    private Integer status;

    @ApiModelProperty("Withdrawal Hash")
    private String hash;

    @ApiModelProperty("Start Add Time")
    private String startAddTime;

    @ApiModelProperty("End Add Time")
    private String endAddTime;

    @ApiModelProperty("Backend Processing Start Time")
    private String startProcessTime;

    @ApiModelProperty("Backend Processing End Time")
    private String endProcessTime;

    @ApiModelProperty("Export (0: No, 1: Yes)")
    private Integer isOut;

    @ApiModelProperty(value = "Account")
    private String account;

    @ApiModelProperty(value = "Member ID")
    private Long memberId;
}
