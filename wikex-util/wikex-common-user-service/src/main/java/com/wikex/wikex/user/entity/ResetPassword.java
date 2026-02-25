package com.wikex.wikex.user.entity;

import lombok.Data;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ResetPassword {
    @NotNull(message = "{ResetPassword.mode.empty}")
    private Integer mode;

    @javax.validation.constraints.Email(message = "{ResetPassword.account.format}")
    private String account;
    @NotNull(message = "{ResetPassword.code.empty}")
    private String code;

    @javax.validation.constraints.NotBlank(message = "{ResetPassword.password.empty}")
    @Length(min = 6, max = 20, message = "{ResetPassword.password.length}")
    private String password;
    @Nullable()
    private String googleCode;

}
