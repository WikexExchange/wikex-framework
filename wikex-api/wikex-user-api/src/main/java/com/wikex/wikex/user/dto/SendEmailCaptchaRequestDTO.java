package com.wikex.wikex.user.dto;

import lombok.Data;

@Data
public class SendEmailCaptchaRequestDTO {
    private String email;
    private CaptchaGeetestDTO captcha;
}
