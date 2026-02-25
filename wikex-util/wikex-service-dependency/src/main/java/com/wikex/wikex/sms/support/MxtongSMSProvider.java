package com.wikex.wikex.sms.support;

import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


@Slf4j
public class MxtongSMSProvider implements SMSProvider {
    private String username;
    private String password;
    private String account;
	private String sign;


    public MxtongSMSProvider(String username, String password, String account,String sign) {
    	this.username = username;
    	this.password = password;
    	this.account = account;
		this.sign = sign;
    }

    public static String getName() {
        return "mxtong";
    }

	@Override
	public MessageResult sendSingleMessage(String mobile, String content) throws Exception {


		return sendMessage(mobile,content);


	}

	@Override
	public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
		String content = formatVerifyCode(verifyCode);
		return sendSingleMessage(mobile, content);
	}
	@Override
	public MessageResult sendMessageByTempId(String mobile, String content, String templateId) throws Exception {
		return null;
	}


	public MessageResult sendMessage(String mobile, String content) throws Exception{
        
		StringBuffer sb = new StringBuffer("http://www.mxtong.cn:8080/GateWay/Services.asmx/DirectSend?");
		sb.append("UserID="+username);
		sb.append("&Account="+account);
		sb.append("&Password="+password);
		sb.append("&Phones="+mobile);
		sb.append("&Content=" 
		+ URLEncoder.encode(
			"Your verification code is " + content + ". It is valid for 10 minutes. If this is not your operation, please ignore it. 【" + this.sign + "】", 
			"UTF-8"
		)
	);
			sb.append("&SendType=1");
		sb.append("&SendTime=");
		sb.append("&PostFixNumber=");
		
		
		URL url = new URL(sb.toString());

		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		
		connection.setRequestMethod("GET");

		
		InputStream is =url.openStream();

		
		String returnStr = convertStreamToString(is);

		
		

		return parseResult(returnStr);
    }

    private MessageResult parseResult(String result) {
        
		
		
		
		
		
		
		
		MessageResult mr = new MessageResult(500, "System Error");
	    if(result.indexOf("<RetCode>Sucess</RetCode>")>1){
		   mr.setCode(0);
		   mr.setMessage("SUCCESS");
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
		
		MessageResult mr = new MessageResult(500, "Not supported");
		return mr;
	}
}
