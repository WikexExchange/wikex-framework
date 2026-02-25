package com.wikex.wikex.p2p.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class AppealApply {
    @NotNull(message = "Missing parameter")
    private String orderSn;

    @NotNull(message = "Appeal reason cannot be empty")
    private String remark;
}
