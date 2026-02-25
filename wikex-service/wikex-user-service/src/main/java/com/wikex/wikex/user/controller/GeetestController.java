package com.wikex.wikex.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.system.GeetestLib;
import com.wikex.wikex.util.IPUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;


@Api(tags = "Geetest")
@RestController
@Slf4j
public class GeetestController extends BaseController {

    @Autowired
    private GeetestLib gtSdk;
    @Value("${water.proof.app.id}")
    private String appId;
    @Value("${water.proof.app.secret.key}")
    private String appSecretKey;
    private static final String url = "https://ssl.captcha.qq.com/ticket/verify";

    private static MultiThreadedHttpConnectionManager connectionManager = null;
    private static int connectionTimeOut = 15000;
    private static int socketTimeOut = 15000;
    private static int maxConnectionPerHost = 500;
    private static int maxTotalConnections = 500;
    private static HttpClient client;

    static {
        connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setConnectionTimeout(connectionTimeOut);
        connectionManager.getParams().setSoTimeout(socketTimeOut);
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
        connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);
        client = new HttpClient(connectionManager);
    }

    @ApiOperation(value = "Captcha")
    @RequestMapping(value = "/start/captcha")
    public String startCaptcha(HttpServletRequest request) {
        String resStr = "{}";
        String userid = "spark";
        // Custom parameters, optional to add
        HashMap<String, String> param = new HashMap<String, String>();
        String ip = IPUtils.getIpAddr(request);
        param.put("user_id", userid); // Website user id
        param.put("client_type", "web"); // web: browser on PC; h5: browser on mobile (including embedded web_view in
                                         // apps); native: integrated via SDK in native apps
        param.put("ip_address", ip); // IP address carried when the user requests validation
        // Perform validation preprocessing
        int gtServerStatus = gtSdk.preProcess(param);
        // Store server status in session
        request.getSession().setAttribute(gtSdk.gtServerStatusSessionKey, gtServerStatus);
        // Store userid in session
        request.getSession().setAttribute("userid", userid);
        resStr = gtSdk.getResponseStr();
        return resStr;
    }

    public Boolean watherProof(String ticket, String randStr, String ip) throws Exception {
        String response = null;
        GetMethod getMethod = null;
        Boolean responseBool = false;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(url).append("?aid=").append(appId)
                    .append("&AppSecretKey=").append(appSecretKey)
                    .append("&Ticket=").append(ticket)
                    .append("&Randstr=").append(randStr)
                    .append("&UserIP=").append(ip);
            getMethod = new GetMethod(sb.toString());
            int code = client.executeMethod(getMethod);
            if (code == 200) {
                response = getMethod.getResponseBodyAsString();
            } else {
                // do nothing
            }
        } catch (HttpException e) {
            // do nothing
        } catch (IOException e) {
            log.error("Network exception occurred", e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
                getMethod = null;
            }
        }

        if (!StringUtils.isEmpty(response)) {
            JSONObject responseJson = JSONObject.parseObject(response);
            String code = responseJson.getString("response");
            if ("1".equals(code)) {
                responseBool = true;
            }
        }
        return responseBool;
    }
}
