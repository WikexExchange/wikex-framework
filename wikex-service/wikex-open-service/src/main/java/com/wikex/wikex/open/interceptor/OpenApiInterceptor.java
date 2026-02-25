package com.wikex.wikex.open.interceptor;

import com.wikex.wikex.user.entity.MemberApiKey;
import com.wikex.wikex.open.util.RedisUtil;
import com.wikex.wikex.user.feign.MemberApiKeyFeign;
import com.wikex.wikex.util.IPUtils;
import com.wikex.wikex.util.MD5;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description: OpenApiInterceptor
 * API interceptor
 * @author Hevin
 * @create: 2019/05/06 14:11
 */
@Slf4j
public class OpenApiInterceptor implements HandlerInterceptor {

    @Autowired
    private MemberApiKeyFeign memberApiKeyService;
    @Autowired
    private RedisUtil redisUtil;

    private static final String API_HOST = "39.100.79.158";
    private static final String SIGNATURE_METHOD = "HmacSHA256";
    private static final String SIGNATURE_VERSION = "2";

    private static final ZoneId ZONE_GMT = ZoneId.of("Z");
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        // Get request parameters
        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String,String> params = new TreeMap<>();
        for (Iterator iterator = parameterMap.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            String[] values =  parameterMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            // Encoding issue fix, use this when encountering garbled text.
            // If mysign and sign are not equal, try using this conversion
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        String accessKeyId = params.get("accessKeyId");
        String apiKey ="";
        if(accessKeyId!=null){
            apiKey = accessKeyId;
        }
        String signature = params.get("signature");
        if(StringUtils.isEmpty(signature)){
            this.ajaxReturn(response,3002,"Signature error, please verify");
            return false;
        }
        if(StringUtils.isEmpty(apiKey)){
            this.ajaxReturn(response,3000,"Parameter error, please verify");
            return false;
        }
        String timestamp = params.get("timestamp");
        if(StringUtils.isEmpty(timestamp)){
            this.ajaxReturn(response,3003,"Timestamp error, please verify");
            return false;
        }

        // Get user info by apiKey
        // Solve service null injection problem
        BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        MemberApiKey memberApiKey = memberApiKeyService.findMemberApiKeyByApiKey(apiKey);
        if(memberApiKey==null){
            this.ajaxReturn(response,3001,"apiKey error, please verify");
            return false;
        }

        // Check IP
        String remoteIp = IPUtils.getIpAddr(request);
        // Limit IP request frequency: once every 10 seconds
        Object o = redisUtil.get(remoteIp);
        if(o!=null){
            this.ajaxReturn(response,3005,"Too many requests, please try later");
            return false;
        }
        redisUtil.set(remoteIp,"limit",10,TimeUnit.SECONDS);

        String ips = memberApiKey.getBindIp();
        if(StringUtils.isNotEmpty(ips)){
            String[] split = ips.split(",");
            List<String> ipList = Arrays.asList(split);
            if(!ipList.contains(remoteIp)){
                this.ajaxReturn(response,3004,"IP error, please verify");
                return false;
            }
        }

        // Create signature
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Remove redundant params
//        params.remove("accessKeyId");
//        params.remove("timestamp");
        params.remove("signature");
        params.remove("signatureMethod");
        params.remove("signatureVersion");

        String  sign= createSignature(params,memberApiKey.getSecretKey());
        if(!signature.equals(sign)){
            this.ajaxReturn(response,3002,"Signature error, please verify");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }

    private static String createSignature(Map<String,String> map,String secretKey) {
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,String> entry:map.entrySet()){
            if(entry.getValue() != null && StringUtils.isNotBlank(entry.getValue()) && !"null".equals(entry.getValue())
                    && !"class".equals(entry.getKey()) && !"data".equals(entry.getKey())){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        // Remove the last "&"
        int lastIdx = result.lastIndexOf("&");
        result = result.substring(0,lastIdx);
        result +=  secretKey;
        try{
            result = MD5.md5(result).toUpperCase();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String encode(String code) {
        try {
            return URLEncoder.encode(code, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void ajaxReturn(HttpServletResponse response, int code, String msg) throws IOException, JSONException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("code", code);
        json.put("message", msg);
        out.print(json.toString());
        out.flush();
        out.close();
    }
}
