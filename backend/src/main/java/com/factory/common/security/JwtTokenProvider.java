package com.factory.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms:900000}") // 15 mins
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}") // 7 days
    private long refreshExpirationMs;

    public String generateAccessToken(String userId, String email, String fullName, String role, String officeId, String state, java.util.List<String> allowedOffices) {
        long now = Instant.now().toEpochMilli();
        long exp = now + accessExpirationMs;

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("id", userId);
        claims.put("name", fullName);
        claims.put("role", role);
        claims.put("officeId", officeId);
        claims.put("state", state);
        claims.put("allowedOffices", allowedOffices);
        claims.put("iat", now / 1000);
        claims.put("exp", exp / 1000);
        claims.put("type", "ACCESS");

        return createSignedToken(claims);
    }

    public String generateRefreshToken(String email) {
        long now = Instant.now().toEpochMilli();
        long exp = now + refreshExpirationMs;

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("iat", now / 1000);
        claims.put("exp", exp / 1000);
        claims.put("type", "REFRESH");

        return createSignedToken(claims);
    }

    public boolean validateToken(String token) {
        try {
            Map<String, Object> claims = parseAndVerifyClaims(token);
            if (claims == null) {
                return false;
            }
            Number expNumber = (Number) claims.get("exp");
            if (expNumber == null) {
                return false;
            }
            long expSeconds = expNumber.longValue();
            long nowSeconds = Instant.now().getEpochSecond();
            return nowSeconds < expSeconds;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Map<String, Object> claims = parseAndVerifyClaims(token);
        return claims != null ? (String) claims.get("sub") : null;
    }

    public Map<String, Object> getClaims(String token) {
        return parseAndVerifyClaims(token);
    }

    private String createSignedToken(Map<String, Object> claims) {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            String headerJson = objectMapper.writeValueAsString(header);
            String payloadJson = objectMapper.writeValueAsString(claims);

            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String contentToSign = encodedHeader + "." + encodedPayload;
            String signature = sign(contentToSign, jwtSecret);

            return contentToSign + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT token", e);
        }
    }

    private Map<String, Object> parseAndVerifyClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String contentToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(contentToSign, jwtSecret);

            if (!MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                log.warn("JWT signature verification failed");
                return null;
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Error parsing JWT claims: {}", e.getMessage());
            return null;
        }
    }

    private String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }
}
