package com.wikex.wikex.user.util;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileReader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.Date;

public class AppleLoginUtil {

    public static String generateClientSecret(String teamId, String keyId, String clientId, String keyPath)
            throws Exception {
        PEMParser pemParser = new PEMParser(new FileReader(keyPath));
        PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
        PrivateKey privateKey = new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
        pemParser.close();

        return Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, keyId)
                .setIssuer(teamId)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
                .setIssuedAt(new Date())
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }
}
