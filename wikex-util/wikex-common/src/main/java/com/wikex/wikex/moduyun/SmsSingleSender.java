package com.wikex.wikex.moduyun;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.List;

public class SmsSingleSender {
    String accesskey;
    String secretkey;

    SmsSenderUtil util = new SmsSenderUtil();

    public SmsSingleSender(String accesskey, String secretkey) throws Exception {
        this.accesskey = accesskey;
        this.secretkey = secretkey;
    }

    public SmsSingleSenderResult send(
            int type,
            String nationCode,
            String phoneNumber,
            String msg,
            String extend,
            String ext) throws Exception {

        if (0 != type && 1 != type) {
            throw new Exception("type " + type + " error");
        }
        if (null == extend) {
            extend = "";
        }
        if (null == ext) {
            ext = "";
        }

        long random = util.getRandom();
        long curTime = System.currentTimeMillis() / 1000;
        String url = "https://live.moduyun.com/sms/v1/sendsinglesms";
        JSONObject data = new JSONObject();

        JSONObject tel = new JSONObject();
        tel.put("nationcode", nationCode);
        tel.put("mobile", phoneNumber);

        data.put("type", type);
        data.put("msg", msg);
        data.put("sig", util.strToHash(String.format(
                "secretkey=%s&random=%d&time=%d&mobile=%s",
                secretkey, random, curTime, phoneNumber)));
        data.put("tel", tel);
        data.put("time", curTime);
        data.put("extend", extend);
        data.put("ext", ext);

        String wholeUrl = String.format("%s?accesskey=%s&random=%d", url, accesskey, random);
        HttpURLConnection conn = util.getPostHttpConn(wholeUrl);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
        wr.write(data.toString());
        wr.flush();

        System.out.println(data.toString());

        StringBuilder sb = new StringBuilder();
        int httpRspCode = conn.getResponseCode();
        SmsSingleSenderResult result;
        if (httpRspCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            System.out.println(sb.toString());
            JSONObject json = JSONObject.parseObject(sb.toString());
            result = util.jsonToSmsSingleSenderResult(json);
        } else {
            result = new SmsSingleSenderResult();
            result.result = httpRspCode;
            result.errMsg = "http error " + httpRspCode + " " + conn.getResponseMessage();
        }

        return result;
    }

    public SmsSingleSenderResult sendWithParam(
            int type,
            String nationcode,
            String phoneNumber,
            String signId,
            String templateId,
            List<String> params,
            String ext) throws Exception {

        if (null == ext) {
            ext = "";
        }

        long random = util.getRandom();
        long curTime = System.currentTimeMillis() / 1000;
        String url = "https://live.moduyun.com/sms/v2/sendsinglesms";
        JSONObject data = new JSONObject();

        JSONObject tel = new JSONObject();
        tel.put("nationcode", nationcode);
        tel.put("mobile", phoneNumber);

        data.put("signId", signId);
        data.put("templateId", templateId);
        data.put("type", type);
        if (params != null && params.size() > 0) {
            data.put("params", util.smsParamsToJSONArray(params));
        }

        data.put("sig", util.strToHash(String.format(
                "secretkey=%s&random=%d&time=%d&mobile=%s",
                secretkey, random, curTime, phoneNumber)));
        data.put("tel", tel);
        data.put("time", curTime);
        data.put("ext", ext);

        String wholeUrl = String.format("%s?accesskey=%s&random=%d", url, accesskey, random);
        HttpURLConnection conn = util.getPostHttpConn(wholeUrl);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
        System.out.println(data.toString());
        wr.write(data.toString());
        wr.flush();

        System.out.println(data.toString());

        StringBuilder sb = new StringBuilder();
        int httpRspCode = conn.getResponseCode();
        SmsSingleSenderResult result;
        if (httpRspCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            System.out.println(sb.toString());
            JSONObject json = JSONObject.parseObject(sb.toString());
            result = util.jsonToSmsSingleSenderResult(json);
        } else {
            result = new SmsSingleSenderResult();
            result.result = httpRspCode;
            result.errMsg = "http error " + httpRspCode + " " + conn.getResponseMessage();
        }

        return result;
    }
}
