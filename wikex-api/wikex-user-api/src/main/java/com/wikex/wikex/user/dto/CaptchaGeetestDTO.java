package com.wikex.wikex.user.dto;

import lombok.Data;

@Data
public class CaptchaGeetestDTO {
    private String lot_number;
    private String captcha_output;
    private String pass_token;
    private String gen_time;
    private String captcha_id;
}
