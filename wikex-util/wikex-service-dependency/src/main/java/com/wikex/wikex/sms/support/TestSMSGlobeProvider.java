package com.wikex.wikex.sms.support;

import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class TestSMSGlobeProvider implements SMSProvider {
    private String ali_accessKeyId; // accessKeyId
    private String ali_accessSecret; // accessSecret


    public TestSMSGlobeProvider(String ali_accessKeyId, String ali_accessSecret) {
        this.ali_accessKeyId = ali_accessKeyId;
        this.ali_accessSecret = ali_accessSecret;
    }

    public static String getName() {
        return "test";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        return sendMessage(mobile,content);
    }

    @Override
    public MessageResult sendMessageByTempId(String mobile, String content, String templateId) throws Exception {
        return null;
    }

    public MessageResult sendMessage(String mobile, String content) throws Exception{

        

        return parseResult(content);

    }

    @Override
    public MessageResult sendInternationalMessage(String content, String phone) throws IOException, DocumentException {

        

        MessageResult messageResult = MessageResult.success();
        messageResult.setMessage(content);
        return messageResult;
    }

    private MessageResult parseResult(String result) {

        MessageResult mr = new MessageResult(0, result);
        return mr;
    }

    
    public String convertStreamToString(InputStream is) {
        StringBuilder sb1 = new StringBuilder();
        byte[] bytes = new byte[4096];
        int size = 0;

        try {
            while ((size = is.read(bytes)) > 0) {
                String str = new String(bytes, 0, size, "UTF-8");
                sb1.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb1.toString();
    }

    @Override
    public MessageResult sendCustomMessage(String mobile, String content) throws Exception {
        
        MessageResult mr = new MessageResult(500, "Alibaba Cloud SMS does not support custom text");
        return mr;
    }
}
