package com.wikex.wikex.sms;


import com.wikex.wikex.util.MessageResult;
import org.springframework.scheduling.annotation.Async;

public interface EmailProvider {
    
    @Async
    MessageResult sendEmail(String email, String code, String subject, String templateName) throws Exception;

}
