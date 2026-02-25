package com.wikex.wikex.screen;

import com.wikex.wikex.constant.CommonStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Transfer Address")
@Data
public class TransferAddressScreen {
    @ApiModelProperty(value = "Transfer Status")
    private CommonStatus start;

    @ApiModelProperty(value = "Transfer Address")
    private String address;

    @ApiModelProperty(value = "Coin")
    private String unit;
}
