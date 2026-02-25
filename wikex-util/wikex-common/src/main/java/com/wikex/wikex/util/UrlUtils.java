package com.wikex.wikex.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class UrlUtils {

    
    public static String replateUrlParameter(String url,String... names){
        for (String name : names) {
            url = url.replaceAll("(&"+name+"=([0-9\\w]+))|("+name+"=([0-9\\w]+)&)|("+name+"=([0-9\\w]+))", "");
        }
        return url;
    }

    
    public static String map2url(String baseUrl,Map<String,Object> searchMap,String... names){
        
        String parm = map2parm(searchMap);
        if(!StringUtils.isEmpty(parm)){
            baseUrl+="?"+parm;
        }
        
        baseUrl = replateUrlParameter(baseUrl,names);
        return baseUrl;
    }

    
    public static String map2parm(Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append("&");
        }
        String parameters = sb.toString();
        if (parameters.endsWith("&")) {
            parameters = StringUtils.substringBeforeLast(parameters ,"&");
        }
        return parameters;
    }
}
