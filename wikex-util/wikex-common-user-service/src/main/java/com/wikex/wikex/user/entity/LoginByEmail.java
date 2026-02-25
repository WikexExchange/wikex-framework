package com.wikex.wikex.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class LoginByEmail {

    @NotBlank(message = "{LoginByEmail.email.null}")
    @Email(message = "{LoginByEmail.email.format}")
    private String email;

    @NotBlank(message = "{LoginByEmail.password.null}")
    @Length(min = 6, max = 20, message = "{LoginByEmail.password.length}")
    private String password;

    @NotBlank(message = "{LoginByEmail.username.null}")
    private String username;

    @NotBlank(message =  "{LoginByEmail.country.null}")
    private String country;

    private String promotion;

    @NotBlank(message = "{LoginByEmail.code.null}")
    private String code;

    private String superPartner ;

}
