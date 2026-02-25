package com.wikex.wikex.user.entity;

import lombok.Data;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ChangePassword {
    @NotBlank(message = "{ChangePassword.mode.null}")
    private int mode;

    @NotBlank(message = "{ChangePassword.code.null}")
    private String code;

    @NotBlank(message = "{ChangePassword.password.null}")
    @Length(min = 6, max = 20, message = "{ChangePassword.password.length}")
    private String password;

    @NotBlank(message = "{ChangePassword.oldPassword.null}")
    @Length(min = 6, max = 20, message = "{ChangePassword.oldPassword.length}")
    private String oldPassword;

    @Nullable()
    private String googleCode;

}
