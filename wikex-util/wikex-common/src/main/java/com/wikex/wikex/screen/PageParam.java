package com.wikex.wikex.screen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Pagination Parameters")
@Data
public class PageParam {

    @ApiModelProperty(value = "Page Number")
    Integer pageNo = 1;

    @ApiModelProperty(value = "Page Size")
    Integer pageSize = 10;
}
