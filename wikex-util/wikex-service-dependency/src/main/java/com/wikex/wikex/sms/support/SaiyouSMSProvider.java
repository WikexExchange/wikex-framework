package com.wikex.wikex.sms.support;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.moduyun.SmsSingleSender;
import com.wikex.wikex.moduyun.SmsSingleSenderResult;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.HttpSend;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SaiyouSMSProvider implements SMSProvider {
    private String username;
    private String password;
    private String sign;
    private String gateway;

    public SaiyouSMSProvider(String username, String password, String sign, String gateway) {
        this.username = username;
        this.password = password;
        this.sign = sign;
        this.gateway = gateway;
    }

    public static String getName() {
        return "saiyou";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        return sendMessage(mobile, content);
    }

    @Override
    public MessageResult sendMessageByTempId(String mobile, String content, String templateId) throws Exception {
        return null;
    }

    public MessageResult sendMessage(String mobile, String content) throws Exception {
        SmsSingleSender singleSender = new SmsSingleSender(username, password);
        SmsSingleSenderResult singleSenderResult;
        String smsContent = "【" + this.sign + "】Your verification code is " + content + ", valid within 10 minutes. If this was not you, please ignore.";
        singleSenderResult = singleSender.send(0, "86", mobile, smsContent, "", "");

        MessageResult mr = new MessageResult(500, "System error");
        if (singleSenderResult.result == 0) {
            mr.setCode(0);
            mr.setMessage("SMS sent successfully!");
        } else {
            mr.setCode(1);
            mr.setMessage("SMS sending failed, please contact the platform!");
        }
        return mr;
    }

    private MessageResult parseResult(String result) {
        JSONObject jsonObject = JSONObject.parseObject(result);
        MessageResult mr = new MessageResult(500, "System error");
        if ("success".equals(jsonObject.getString("status"))) {
            mr.setCode(0);
            mr.setMessage("SMS sent successfully!");
        } else {
            mr.setCode(1);
            mr.setMessage("SMS sending failed, please contact the platform!");
        }
        return mr;
    }

    /**
     * Convert response stream to UTF-8 format.
     */
    public String convertStreamToString(InputStream is) {
        StringBuilder sb1 = new StringBuilder();
        byte[] bytes = new byte[4096];
        int size;
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
        Map<String, String> params = new HashMap<>();
        params.put("appid", username);
        params.put("to", mobile);
        String smsContent = "【" + this.sign + "】" + content;
        params.put("content", smsContent);
        params.put("signature", password);

        String returnStr = HttpSend.post(gateway, params);
        return parseResult(returnStr);
    }
}
