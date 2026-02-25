package com.wikex.wikex.admin.vo;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@ToString
public class AutomainSetPassword {

    @NotNull(message = "Protocol ID must not be null")
    private Integer protocol;

    @NotNull(message = "Password must not be null")
    private String password;
}
