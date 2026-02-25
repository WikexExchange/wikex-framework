package com.wikex.wikex.swap.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Component
@Slf4j
public class IFindUtils {

    @Value("${ifind.refresh_token}")
    private String refreshToken;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    public JSONObject getKLineMinute(String codes,String startTime,String endTime,String interval) throws IOException {
        
        
        
        String url = "https://ft.10jqka.com.cn/api/v1/high_frequency";
        
        RestTemplate restTemplate = new RestTemplate();

        
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access_token", getToken());
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "gzip, deflate, br");

        Map<String,Object> params = new HashMap<>();
        params.put("codes",codes);
        params.put("indicators","open,close,high,low,volume,amount");
        params.put("starttime",startTime);
        params.put("endtime",endTime);
        Map<String,Object> functionpara = new HashMap<>();
        functionpara.put("Interval",interval);
        functionpara.put("Fill","Original");
        params.put("functionpara",functionpara);

        

        
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
        String body = "";
        List<String> encodings = response.getHeaders().get("Content-Encoding");
        if (encodings!=null && "gzip".equals(encodings.get(0))) {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(response.getBody()))) {
                byte[] decompressedBytes = StreamUtils.copyToByteArray(gzipInputStream);
                
                body = new String(decompressedBytes);
            }
        }else {
            body = new String(response.getBody(), StandardCharsets.UTF_8);
        }
        JSONObject result = null;
        if(response.getStatusCodeValue()==200){
             result = JSON.parseObject(body);
        }
        return result;
    }


    public String getToken() throws IOException {
        String key = "IFind.Token";
        ValueOperations<String, String> opt = redisTemplate.opsForValue();
        String token = opt.get(key);
        if(token!=null){
            return token;
        }
        
        String url = "https://ft.10jqka.com.cn/api/v1/get_access_token";
        
        RestTemplate restTemplate = new RestTemplate();

        
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("refresh_token", refreshToken);
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "gzip, deflate, br");

        Map<String,Object> params = new HashMap<>();

        
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        String accessToken = "";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if(response.getStatusCodeValue()==200){
            
            JSONObject parse = JSON.parseObject(response.getBody());
            JSONObject data = parse.getJSONObject("data");
            accessToken = data.getString("access_token");
            opt.set(key,accessToken,6, TimeUnit.DAYS);
        }
        return  accessToken;
    }

    public JSONObject getRealTimePlate(String codes) throws IOException {
        
        
        
        String url = "https://ft.10jqka.com.cn/api/v1/real_time_quotation";
        
        RestTemplate restTemplate = new RestTemplate();

        
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access_token", getToken());
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "gzip, deflate, br");

        Map<String,Object> params = new HashMap<>();
        params.put("codes",codes);
        String indicators = "latest,volume,bid1,bid2,bid3,bid4,bid5,bid6,bid7,bid8,bid9,bid10,ask1,ask2,ask3,ask4,ask5,ask6,ask7,ask8,ask9,ask10";
        indicators += "bidSize1,bidSize2,bidSize3,bidSize4,bidSize5,bidSize6,bidSize7,bidSize8,bidSize9,bidSize10,askSize1,askSize2,askSize3,askSize4,askSize5,askSize6,askSize7,askSize8,askSize9,askSize10";
        params.put("indicators",indicators);
        params.put("currency","mhb");

        
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
        String body = "";
        List<String> encodings = response.getHeaders().get("Content-Encoding");
        if (encodings!=null && "gzip".equals(encodings.get(0))) {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(response.getBody()))) {
                byte[] decompressedBytes = StreamUtils.copyToByteArray(gzipInputStream);
                
                body = new String(decompressedBytes);
            }
        }else {
            body = new String(response.getBody(), StandardCharsets.UTF_8);
        }
        JSONObject result = null;
        if(response.getStatusCodeValue()==200){
            result = JSON.parseObject(body);
        }
        return result;
    }
}
