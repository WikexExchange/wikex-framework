package com.wikex.wikex.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class LoginBySocial {
    @NotBlank(message = "{LoginBySocial.idToken.null}")
    private String idToken;

    // private String googleCode;
}
