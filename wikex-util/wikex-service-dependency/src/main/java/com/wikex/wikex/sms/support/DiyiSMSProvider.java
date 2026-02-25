package com.wikex.wikex.sms.support;

import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Slf4j
public class DiyiSMSProvider implements SMSProvider {
    private String username;
    private String password;
    private String sign;

    public DiyiSMSProvider(String username, String password, String sign) {
        this.username = username;
        this.password = password;
        this.sign = sign;
    }

    public static String getName() {
        return "diyi";
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
        StringBuffer sb = new StringBuffer("http://sms.1xinxi.cn/asmx/smsservice.aspx?");

        sb.append("name=" + this.username);
        sb.append("&pwd=" + this.password);
        sb.append("&mobile=" + mobile);
        sb.append("&content=" + URLEncoder.encode("Your verification code is " + content + ", valid for 10 minutes. If this was not your operation, please ignore it.", "UTF-8"));
        sb.append("&stime=");
        sb.append("&sign=" + URLEncoder.encode(this.sign, "UTF-8"));
        sb.append("&type=pt&extno=");

        URL url = new URL(sb.toString());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        InputStream is = url.openStream();
        String returnStr = convertStreamToString(is);

        return parseResult(returnStr);
    }

    private MessageResult parseResult(String result) {
        String[] resArr = result.split(",");
        MessageResult mr = new MessageResult(500, "System error");
        mr.setCode(Integer.parseInt(resArr[0]));
        mr.setMessage(resArr[5]);
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
        StringBuffer sb = new StringBuffer("http://sms.1xinxi.cn/asmx/smsservice.aspx?");

        sb.append("name=" + this.username);
        sb.append("&pwd=" + this.password);
        sb.append("&mobile=" + mobile);
        sb.append("&content=" + URLEncoder.encode(content, "UTF-8"));
        sb.append("&stime=");
        sb.append("&sign=" + URLEncoder.encode(this.sign, "UTF-8"));
        sb.append("&type=pt&extno=");

        URL url = new URL(sb.toString());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        InputStream is = url.openStream();
        String returnStr = convertStreamToString(is);

        return parseResult(returnStr);
    }
}
