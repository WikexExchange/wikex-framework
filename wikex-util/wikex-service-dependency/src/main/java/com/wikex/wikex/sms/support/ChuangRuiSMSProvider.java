package com.wikex.wikex.sms.support;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.util.List;

@Slf4j
public class ChuangRuiSMSProvider implements SMSProvider {

    private String gateway;
    private String username;
    private String password;
    private String sign;
    private String accesskey;
    private String accessSecret;
    private String smsName;
    private String templateId;

    public ChuangRuiSMSProvider(String gateway, String username, String password, String sign, String accesskey,
                                String accessSecret, String templateId, String smsName) {
        this.gateway = gateway;
        this.username = username;
        this.password = password;
        this.sign = sign;
        this.accesskey = accesskey;
        this.accessSecret = accessSecret;
        this.templateId = templateId;
        this.smsName = smsName;
    }

    public static String getName() {
        return "chuangrui";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        if ("chuangrui".equals(smsName)) {
            return sendMessage(mobile, content, this.templateId);
        } else if ("gongxintong".equals(smsName)) {
            return send2Method(mobile, content);
        }
        return null;
    }

    @Override
    public MessageResult sendMessageByTempId(String mobile, String content, String templateId) throws Exception {
        if ("chuangrui".equals(smsName)) {
            return sendMessage(mobile, content, templateId);
        } else if ("gongxintong".equals(smsName)) {
            return sendByOder(mobile, content);
        }
        return null;
    }

    public MessageResult sendMessage(String mobile, String content, String templateId) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod("http://api.1cloudsp.com/api/v2/single_send");
        postMethod.getParams().setContentCharset("UTF-8");
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

        NameValuePair[] data = {
                new NameValuePair("accesskey", this.accesskey),
                new NameValuePair("secret", this.accessSecret),
                new NameValuePair("sign", this.sign),
                new NameValuePair("templateId", templateId),
                new NameValuePair("mobile", mobile),
                new NameValuePair("content", URLEncoder.encode(content, "utf-8"))
        };
        postMethod.setRequestBody(data);

        int statusCode = httpClient.executeMethod(postMethod);
        System.out.println("statusCode: " + statusCode + ", body: "
                + postMethod.getResponseBodyAsString());

        log.info(" mobile : " + mobile + "content : " + content);
        log.info("statusCode: " + statusCode + ", body: "
                + postMethod.getResponseBodyAsString());
        return parseResult(postMethod.getResponseBodyAsString());
    }

    private MessageResult parseResult(String result) {
        JSONObject parts = JSONObject.parseObject(result);
        MessageResult mr = new MessageResult(500, "System error");
        mr.setCode(Integer.parseInt(parts.getString("code")));
        mr.setMessage(parts.getString("msg"));
        return mr;
    }

    public MessageResult sendByOder(String mobile, String content) throws Exception {
        HttpClient httpClient = new HttpClient();

        JSONObject json = new JSONObject();
        json.put("id", 1);
        json.put("method", "send");
        JSONObject params = new JSONObject();
        params.put("userid", this.accesskey);
        params.put("password", this.accessSecret);
        JSONObject[] phoneSend = new JSONObject[1];
        JSONObject submit = new JSONObject();
        submit.put("content", "Hello, " + StringUtils.substringBefore(content, "#")
                + " has a new order, the other party's username is "
                + StringUtils.substringAfterLast(content, "#")
                + ", please log in to the system and handle it promptly. [KaiYuan]");
        submit.put("phone", mobile);

        phoneSend[0] = submit;
        params.put("submit", phoneSend);
        json.put("params", params);

        String url = "http://112.74.139.4:8002/sms3_api/jsonapi/jsonrpc2.jsp?"
                + URLEncoder.encode(json.toJSONString(), "UTF-8");
        GetMethod getMethod = new GetMethod(url);

        int code = httpClient.executeMethod(getMethod);

        return parse2Result(getMethod.getResponseBodyAsString());
    }

    public MessageResult send2Method(String mobile, String content) throws Exception {
        HttpClient httpClient = new HttpClient();

        JSONObject json = new JSONObject();
        json.put("id", 1);
        json.put("method", "send");
        JSONObject params = new JSONObject();
        params.put("userid", this.accesskey);
        params.put("password", this.accessSecret);
        JSONObject[] phoneSend = new JSONObject[1];
        JSONObject submit = new JSONObject();
        submit.put("content", "Your verification code is " + content
                + ", valid for 10 minutes. If this was not your operation, please ignore it. [KaiYuan]");
        submit.put("phone", mobile);

        phoneSend[0] = submit;
        params.put("submit", phoneSend);
        json.put("params", params);

        String url = "http://112.74.139.4:8002/sms3_api/jsonapi/jsonrpc2.jsp?"
                + URLEncoder.encode(json.toJSONString(), "UTF-8");
        GetMethod getMethod = new GetMethod(url);

        int code = httpClient.executeMethod(getMethod);

        return parse2Result(getMethod.getResponseBodyAsString());
    }

    private MessageResult parse2Result(String result) {
        JSONObject jsonObject = JSONObject.parseObject(result);
        MessageResult mr = new MessageResult(500, "System error");
        List<JSONObject> jsonResult = (List<JSONObject>) jsonObject.get("result");
        JSONObject mess = jsonResult.get(0);
        mr.setCode(Integer.parseInt(mess.getString("return")));
        mr.setMessage(mess.getString("info"));
        return mr;
    }

    @Override
    public MessageResult sendCustomMessage(String mobile, String content) throws Exception {
        return null;
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        return null;
    }

    @Override
    public MessageResult sendInternationalMessage(String content, String phone) throws Exception {
        return null;
    }
}
