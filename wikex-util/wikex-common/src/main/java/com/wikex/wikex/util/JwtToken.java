package com.wikex.wikex.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JwtToken {

    private static final String DEFAULT_SECRET = "5DC1E67EED14944915D2255C62AA7";

    public static String createToken(Map<String, Object> dataMap) {
        return createToken(dataMap, null, null);
    }

    public static String createToken(Map<String, Object> dataMap, String secret, String sub) {

        if (StringUtils.isEmpty(secret)) {
            secret = DEFAULT_SECRET;
        }

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withClaim("body", dataMap)
                .withIssuer("wikex-exchange")
                .withSubject(sub != null ? sub : "JWT Token")
                .withAudience("member")
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)))
                .withNotBefore(new Date(System.currentTimeMillis()))
                .withIssuedAt(new Date())
                .withJWTId(UUID.randomUUID().toString().replace("-", ""))
                .sign(algorithm);
    }

    public static String createRefreshToken(String secret, String sub, Long expirationsInSecond) {
        if (StringUtils.isEmpty(secret)) {
            secret = DEFAULT_SECRET;
        }
        if (expirationsInSecond == 0) {
            expirationsInSecond = 24 * 60 * 60L;
        }
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("memberId", sub);
        String refreshToken = JWT.create()
                .withClaim("body", dataMap)
                .withSubject(sub != null ? sub : "JWT Refresh Token")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expirationsInSecond)))
                .sign(algorithm);
        return refreshToken;
    }

    public static Map<String, Object> parseToken(String token) {
        return parseToken(token, null);
    }

    public static Map<String, Object> parseToken(String token, String secret) {

        if (StringUtils.isEmpty(secret)) {
            secret = DEFAULT_SECRET;
        }

        Algorithm algorithm = Algorithm.HMAC256(secret);

        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT jwt = verifier.verify(token);
        Map<String, Object> bodyData = jwt.getClaim("body").as(Map.class);
        return bodyData;
    }

    public static void main(String[] args) throws InterruptedException {

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("name", "wikex");
        dataMap.put("address", "vietnam");

        String token = createToken(dataMap);
        System.out.println(token);

        TimeUnit.SECONDS.sleep(1);

        Map<String, Object> stringObjectMap = parseToken(token);
        System.out.println(stringObjectMap);
    }
}
