package com.wikex.wikex.sms;

import com.wikex.wikex.util.MessageResult;

public interface SMSProvider {
    
    MessageResult sendSingleMessage(String mobile, String content) throws Exception;

    
    MessageResult sendMessageByTempId(String mobile, String content,String templateId) throws Exception;

    
    MessageResult sendCustomMessage(String mobile, String content) throws Exception;

    
    default MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        return sendSingleMessage(mobile, formatVerifyCode(verifyCode));
    }

    
    default String formatVerifyCode(String code) {
        return String.format("%s", code);
    }

    
    default MessageResult sendInternationalMessage(String content, String phone) throws Exception {
        return sendSingleMessage(phone, content);
    }
}
