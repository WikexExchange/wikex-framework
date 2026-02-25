package com.wikex.wikex.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Coin Protocol DTO")
@Data
public class CoinprotocolDTO {
    @ApiModelProperty(value = "Protocol")
    private Integer protocol;

    @ApiModelProperty(value = "Protocol Name")
    private String protocolName;
}

