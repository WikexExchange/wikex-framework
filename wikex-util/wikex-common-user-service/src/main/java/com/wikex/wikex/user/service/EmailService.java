package com.wikex.wikex.user.service;

import com.aliyuncs.exceptions.ClientException;
import freemarker.template.TemplateException;
import org.springframework.scheduling.annotation.Async;

import javax.mail.MessagingException;
import java.io.IOException;

public interface EmailService {

    @Async
    void sentEmailAddCode(String email, String lang) throws Exception;

    @Async
    void sentBindEmailCode(String email, String lang) throws Exception;

    @Async
    void sentResetPassword(String email, String lang) throws Exception;

    @Async
    void sentSetupPassword(String email, String lang) throws Exception;

    @Async
    void sentChangePassword(String email, String lang) throws Exception;

    @Async
    void sentUntieEmailCode(String email, String lang) throws Exception;

    @Async
    void sentUpdateEmailCode(String email, String lang) throws Exception;

    @Async
    void sentRegEmailCode(String email, String lang) throws Exception;

    @Async
    void sentEmailWelcome(String email, String lang) throws Exception;

    @Async
    void sentEmailDepositCreated(String email, String lang) throws Exception;

    @Async
    void sentEmailDepositSuccess(String email, String lang) throws Exception;

    @Async
    void sendEnable2FA(String email, String lang) throws Exception;

    @Async
    void sendDisable2FA(String email, String lang) throws Exception;

    @Async
    void sendBindGoogleEmail(String email, String lang) throws Exception;

    @Async
    void sendBindAppleEmail(String email, String lang) throws Exception;

    @Async
    void sendUnbindGoogleEmail(String email, String lang) throws Exception;

    @Async
    void sendUnbindAppleEmail(String email, String lang) throws Exception;

    boolean checkCode4ChangePassword(String account, String code);

    boolean checkCode4forgetPassword(String account, String code);

    boolean checkCode4SetupPassword(String account, String code);

    boolean checkCode4LinkGoogle(String account, String code);

    boolean checkCode4LinkApple(String account, String code);

    boolean checkCode4UnlinkGoogle(String account, String code);

    boolean checkCode4UnlinkApple(String account, String code);

    boolean checkCode4Disable2FA(String account, String code);

    boolean checkCode4Reg(String email, String code);

    boolean checkVerificationCode4Email(String email, String code, String type);
}
