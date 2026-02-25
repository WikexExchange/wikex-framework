package com.wikex.wikex.sms.support;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class AliyunSMSGlobeProvider implements SMSProvider {
    private String ali_accessKeyId;
    private String ali_accessSecret;

    public AliyunSMSGlobeProvider(String ali_accessKeyId, String ali_accessSecret) {
        this.ali_accessKeyId = ali_accessKeyId;
        this.ali_accessSecret = ali_accessSecret;
    }

    public static String getName() {
        return "aliyun";
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
        DefaultProfile profile = DefaultProfile.getProfile("ap-southeast-1", ali_accessKeyId, ali_accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.ap-southeast-1.aliyuncs.com");
        request.setSysVersion("2018-05-01");
        request.setSysAction("SendMessageToGlobe");
        request.putQueryParameter("To", mobile);
        request.putQueryParameter("Message", content);

        try {
            CommonResponse response = client.getCommonResponse(request);
            String returnStr = response.getData();
            return parseResult(returnStr);
        } catch (ClientException e) {
            MessageResult mr = new MessageResult(500, "System error");
            e.printStackTrace();
            return mr;
        }
    }

    private MessageResult parseResult(String result) {
        JSONObject jsonObject = JSONObject.parseObject(result);

        MessageResult mr = new MessageResult(500, "System error");
        if (jsonObject.getString("ResponseCode").equals("OK")) {
            mr.setCode(0);
            mr.setMessage("SMS sent successfully!");
        } else {
            mr.setCode(1);
            mr.setMessage("SMS sending failed, please contact the platform!");
        }
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
        MessageResult mr = new MessageResult(500, "Aliyun SMS does not support custom text");
        return mr;
    }
}
