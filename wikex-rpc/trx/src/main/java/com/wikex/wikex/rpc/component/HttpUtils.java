package com.wikex.wikex.rpc.component;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpUtils {

    /**
     * Execute a POST request
     *
     * @param url   target URL
     * @param param request parameters (JSON string)
     * @param apiKey API key for authentication
     * @return ResponseEntity containing the server response
     */
    public static ResponseEntity<String> postForEntity(String url, String param, String apiKey) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("TRON-PRO-API-KEY", apiKey);
        HttpEntity<String> request = new HttpEntity<>(param, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(url, request, String.class);
        // System.out.println("url:" + url + ",param:" + param + ",result:" + result.getBody());
        return result;
    }

    /**
     * Execute a GET request
     *
     * @param url target URL
     * @return Response body as a String
     */
    public static String getForEntity(String url) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        // System.out.println("url:" + url + ",result:" + result.getBody());
        return result.getBody();
    }
}
