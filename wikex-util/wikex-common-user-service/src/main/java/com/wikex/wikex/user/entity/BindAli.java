package com.wikex.wikex.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class BindAli {
    @NotBlank(message = "{BindAli.realName.null}")
    private String realName;
    @NotBlank(message = "{BindAli.ali.null}")
    private String ali;
    @NotBlank(message = "{BindAli.jyPassword.null}")
    private String jyPassword;
    private String qrCodeUrl;
}
