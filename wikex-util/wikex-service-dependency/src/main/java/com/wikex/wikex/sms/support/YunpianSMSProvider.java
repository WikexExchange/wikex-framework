package com.wikex.wikex.sms.support;

import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.HttpSend;
import com.wikex.wikex.util.MessageResult;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


@Slf4j
public class YunpianSMSProvider implements SMSProvider {

    private String gateway;
    private String apikey;

    public YunpianSMSProvider(String gateway, String apikey) {
        this.gateway = gateway;
        this.apikey = apikey;
    }

    private static Pattern RESPONSE_PATTERN = Pattern.compile("<response><error>(-?\\d+)</error><message>(.*[\\\\u4e00-\\\\u9fa5]*)</message></response>");


    public static String getName() {
        return "yunpian";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws UnirestException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("text", content);
        params.put("mobile", mobile);
        
        String resultXml= HttpSend.yunpianPost(gateway, params);
        
        return parseXml(resultXml);
    }

    @Override
    public MessageResult sendMessageByTempId(String mobile, String content, String templateId) throws Exception {
        return null;
    }

    @Override
    public MessageResult sendInternationalMessage(String mobile, String content) throws IOException, DocumentException {
        content = String.format("【KaiYuan】Your verification code is %s", content);
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("text", content);
        params.put("mobile", mobile);
        String resultXml= HttpSend.yunpianPost(gateway, params);
        
        return parseXml(resultXml);
    }

    
    @Override
    public String formatVerifyCode(String code) {
        return String.format("【KaiYuan】Your verification code is: %s. Please fill it out according to the instructions on the page and do not disclose it to others.", code);
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        String content = formatVerifyCode(verifyCode);
        return sendSingleMessage(mobile, content);
    }

    private MessageResult parseXml(String xml) {
        JSONObject myJsonObject = JSONObject.fromObject(xml);
        int code = myJsonObject.getInt("code");
        MessageResult result = new MessageResult(500, "System Error");
        if(code == 0)
        {
            result.setCode(code);
            result.setMessage(myJsonObject.getString("msg"));
        }
        return result;
    }

	@Override
	public MessageResult sendCustomMessage(String mobile, String content) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
