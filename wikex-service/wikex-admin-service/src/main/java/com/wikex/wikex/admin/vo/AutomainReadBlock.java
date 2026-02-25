package com.wikex.wikex.admin.vo;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@ToString
public class AutomainReadBlock {

    @NotNull(message = "协议ID不得为空")
    private Integer protocol;

    @NotNull(message = "高度不能为null")
    private Long blockHeight;
}
