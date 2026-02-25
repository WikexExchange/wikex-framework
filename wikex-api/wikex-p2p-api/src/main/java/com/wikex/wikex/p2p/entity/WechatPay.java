package com.wikex.wikex.p2p.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;


@ApiModel(value = "WeChat Information")
@Data
@Embeddable
public class WechatPay implements Serializable {
    private static final long serialVersionUID = 1511509989072675896L;
    
    @ApiModelProperty(value = "WeChat ID")
    private String wechat;

    
    @ApiModelProperty(value = "WeChat payment QR code")
    private String qrWeCodeUrl;
}
