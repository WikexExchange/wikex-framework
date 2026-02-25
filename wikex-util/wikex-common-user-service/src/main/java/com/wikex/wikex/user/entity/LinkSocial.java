package com.wikex.wikex.user.entity;

import lombok.Data;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class LinkSocial {
    @NotBlank(message = "{LinkSocial.idToken.null}")
    private String idToken;

    @NotBlank(message = "{LinkSocial.code.null}")
    private String code;

    @Nullable()
    private String googleCode;
}
