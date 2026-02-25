package com.wikex.wikex.user.service;

import org.springframework.scheduling.annotation.Async;

import com.wikex.wikex.user.dto.CaptchaGeetestDTO;

public interface CaptchaService {
    @Async
    public boolean verifyCaptcha(CaptchaGeetestDTO captcha);
}