package com.wikex.wikex.earn.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Activity query parameters")
@Data
public class ActivityParam {
    @ApiModelProperty(value = "Page number")
    Integer pageNo = 1;

    @ApiModelProperty(value = "Number of items per page")
    Integer pageSize = 10;

    @ApiModelProperty(value = "Coin unit")
    String unit;

    /**
     * Status: 0 - unavailable, 1 - available
     */
    private Integer status = 1;
}
