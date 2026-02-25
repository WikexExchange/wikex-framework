package com.wikex.wikex.user.service.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.wikex.wikex.user.dto.CaptchaGeetestDTO;
import com.wikex.wikex.user.service.CaptchaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CapchaServiceImpl implements CaptchaService {
    private static final String API_SERVER = "http://gcaptcha4.geetest.com";
    private static final HashMap<String, String> CAPTCHA_KEY_PAIRS = new HashMap<String, String>() {
        {
            put("c239c222b9857d5373614b7134530aad", "8d7391fb976bebd99bef7411fbb07e43");
            put("10f9a5c46d7e0fdef3e949a1303df27c", "7da6747b8f113c74162af1495ab2bae3");
        }
    };
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public boolean verifyCaptcha(CaptchaGeetestDTO captcha) {
        // Input validation
        if (captcha == null || captcha.getCaptcha_id() == null || captcha.getCaptcha_id().isEmpty()
                || captcha.getPass_token() == null || captcha.getPass_token().isEmpty()) {
            return false;
        }
        String captchaKey = CAPTCHA_KEY_PAIRS.get(captcha.getCaptcha_id());
        if (captchaKey == null) {
            log.warn("Invalid captcha_id: {}", captcha.getCaptcha_id());
            return false;
        }
        // Build URL
        String url = API_SERVER + "/validate" + "?captcha_id=" + captcha.getCaptcha_id();
        // Generate sign_token using HMAC-SHA256
        String signToken;
        try {
            signToken = hmacSha256Encode(captcha.getLot_number(), captchaKey);
        } catch (Exception e) {
            log.error("Error generating sign token", e);
            return false;
        }

        // Prepare request parameters
        Map<String, String> paramDict = new HashMap<>();
        paramDict.put("lot_number", captcha.getLot_number());
        paramDict.put("captcha_output", captcha.getCaptcha_output());
        paramDict.put("pass_token", captcha.getPass_token());
        paramDict.put("gen_time", captcha.getGen_time());
        paramDict.put("sign_token", signToken);

        // Send HTTP POST request
        String responseBody = httpPost(url, paramDict);
        if (responseBody == null) {
            // When Geetest server exceptions occur, let request pass to avoid interrupting
            // business
            log.warn("Captcha verification failed: no response from server");
            return false;
        }
        log.info("Captcha verification response: {}", responseBody);
        try {
            // Parse JSON response
            Map<String, Object> resDict = objectMapper.readValue(responseBody, Map.class);
            String result = (String) resDict.get("result");
            return "success".equals(result);
        } catch (Exception e) {
            log.error("Error parsing captcha verification response", e);
            return false;
        }
    }

    // HMAC-SHA256 encoding method
    private String hmacSha256Encode(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // HTTP POST method
    private String httpPost(String urlStr, Map<String, String> params) {
        HttpURLConnection conn = null;
        try {
            // Convert params to URL-encoded form
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formBody.length() > 0) {
                    formBody.append("&");
                }
                formBody.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
            }

            // Create connection
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Write request body
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(formBody.toString());
                wr.flush();
            }

            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
