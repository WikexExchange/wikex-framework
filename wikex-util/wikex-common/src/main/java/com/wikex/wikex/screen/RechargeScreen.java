package com.wikex.wikex.screen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Recharge")
@Data
public class RechargeScreen extends PageParam {

    @ApiModelProperty(value = "Recipient Address")
    private String address;

    @ApiModelProperty(value = "Protocol")
    private Integer protocol;

    @ApiModelProperty(value = "Coin Name")
    private String coinname;

    @ApiModelProperty(value = "Export (0: No, 1: Yes)")
    private Integer isOut;
}
