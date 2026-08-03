package com.audit.dgi.validateur_dgi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(@Value("${app.jwt.secret:change-me-please}") String secret,
                            @Value("${app.jwt.validity:86400000}") long validityInMilliseconds) {
        byte[] keyBytes;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            keyBytes = md.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            keyBytes = java.util.Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 32);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(String email, String role, Long companyId) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("companyId", companyId);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}

