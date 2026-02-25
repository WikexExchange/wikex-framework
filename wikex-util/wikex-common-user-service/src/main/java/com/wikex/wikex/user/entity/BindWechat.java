package com.wikex.wikex.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class BindWechat {
    @NotBlank(message = "{BindWechat.realName.null}")
    private String realName;
    @NotBlank(message = "{BindWechat.wechat.null}")
    private String wechat;
    @NotBlank(message = "{BindWechat.jyPassword.null}")
    private String jyPassword;
    private String qrCodeUrl;
}
