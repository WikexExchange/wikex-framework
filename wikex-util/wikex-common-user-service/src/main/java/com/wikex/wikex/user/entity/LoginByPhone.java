package com.wikex.wikex.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class LoginByPhone {
    
    /*@Pattern(regexp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-9])|(147))\\d{8}$", message = "{LoginByPhone.phone.pattern}")*/
    @NotBlank(message = "{LoginByPhone.phone.null}")
    private String phone;

    @NotBlank(message = "{LoginByPhone.password.null}")
    @Length(min = 6, max = 20, message = "{LoginByPhone.password.length}")
    private String password;

    @NotBlank(message = "{LoginByPhone.username.null}")
    @Length(min = 3, max = 20, message = "{LoginByPhone.username.length}")
    private String username;

    @NotBlank(message = "{LoginByPhone.country.null}")
    private String country;


    @NotBlank(message = "{LoginByPhone.code.null}")
    private String code;

    private String promotion;

    private String superPartner ;
    private String validate ;
    private String ticket ;
    private  String randStr ;

}
