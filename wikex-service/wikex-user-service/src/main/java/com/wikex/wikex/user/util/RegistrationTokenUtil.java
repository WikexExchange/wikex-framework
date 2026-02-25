// package com.wikex.wikex.user.util;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;

// import javax.crypto.SecretKey;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;

// @Component
// public class RegistrationTokenUtil {

// @Value("${jwt.secret}")
// private String jwtSecret;

// @Value("${jwt.expiration.registration}") // 24 hours in milliseconds
// private long tokenExpirationTime;

// // Generate registration token for new user
// public String generateToken(String email, String googleSub) {
// Map<String, Object> claims = new HashMap<>();
// claims.put("email", email);
// claims.put("googleSub", googleSub);
// claims.put("type", "registration");

// return createToken(claims, email);
// }

// // Generate registration token
// public String generateToken(String email, String googleSub, Map<String,
// Object> additionalClaims) {
// Map<String, Object> claims = new HashMap<>();
// claims.put("email", email);
// claims.put("googleSub", googleSub);
// claims.put("type", "registration");

// if (additionalClaims != null) {
// claims.putAll(additionalClaims);
// }

// return createToken(claims, email);
// }

// // Verify and extract token payload
// public Map<String, String> verifyToken(String token) {
// try {
// SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
// Claims claims = Jwts.parserBuilder()
// .setSigningKey(key)
// .build()
// .parseClaimsJws(token)
// .getBody();

// // Verify token type
// String type = (String) claims.get("type");
// if (!"registration".equals(type)) {
// return null;
// }

// Map<String, String> result = new HashMap<>();
// result.put("email", (String) claims.get("email"));
// result.put("googleSub", (String) claims.get("googleSub"));
// result.put("type", type);

// return result;
// } catch (Exception e) {
// // Token invalid, expired, or signature mismatch
// System.err.println("Token verification failed: " + e.getMessage());
// return null;
// }
// }

// // Check if token is still valid (not expired)
// public boolean isTokenValid(String token) {
// try {
// SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
// Jwts.parserBuilder()
// .setSigningKey(key)
// .build()
// .parseClaimsJws(token);
// return true;
// } catch (Exception e) {
// return false;
// }
// }

// // Create JWT token with claims
// private String createToken(Map<String, Object> claims, String subject) {
// SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

// Date now = new Date();
// Date expiryDate = new Date(now.getTime() + tokenExpirationTime);

// return Jwts.builder()
// .setClaims(claims)
// .setSubject(subject)
// .setIssuedAt(now)
// .setExpiration(expiryDate)
// .signWith(key, SignatureAlgorithm.HS256)
// .compact();
// }
// }
