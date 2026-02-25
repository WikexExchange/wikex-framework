package com.wikex.wikex.moduyun;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class SmsVoiceVerifyCodeSender {
    String accesskey;
    String secretkey;
    String url = "https://live.moduyun.com/sms/v1/sendvoice";
    SmsSenderUtil util = new SmsSenderUtil();

    public SmsVoiceVerifyCodeSender(String accesskey, String secretkey) {
        this.accesskey = accesskey;
        this.secretkey = secretkey;
    }

    public SmsVoiceVerifyCodeSenderResult send(
            String nationCode,
            String phoneNumber,
            String msg,
            int playtimes,
            String ext) throws Exception {

        if (null == ext) {
            ext = "";
        }

        long random = util.getRandom();
        long curTime = System.currentTimeMillis() / 1000;

        JSONObject data = new JSONObject();

        JSONObject tel = new JSONObject();
        tel.put("nationcode", nationCode);
        tel.put("mobile", phoneNumber);

        data.put("tel", tel);
        data.put("msg", msg);
        data.put("playtimes", playtimes);
        data.put("sig", util.strToHash(
                String.format("secretkey=%s&random=%d&time=%d&mobile=%s", secretkey, random, curTime, phoneNumber)));
        data.put("time", curTime);
        data.put("ext", ext);

        String wholeUrl = String.format("%s?accesskey=%s&random=%d", url, accesskey, random);
        HttpURLConnection conn = util.getPostHttpConn(wholeUrl);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
        System.out.println(data.toString());
        wr.write(data.toString());
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int httpRspCode = conn.getResponseCode();
        SmsVoiceVerifyCodeSenderResult result;
        if (httpRspCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            br.close();
            JSONObject json = JSONObject.parseObject(sb.toString());
            result = util.jsonToSmsSingleVoiceSenderResult(json);
        } else {
            result = new SmsVoiceVerifyCodeSenderResult();
            result.result = httpRspCode;
            result.errmsg = "http error " + httpRspCode + " " + conn.getResponseMessage();
        }

        return result;
    }
}
