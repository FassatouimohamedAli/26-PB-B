package com.example.pfebtk.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    //private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    @Value("${jwt.secret}")
    private String secret;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    //private final long expiration = TimeUnit.HOURS.toMillis(12);
    private final long expiration = 1000 * 60  * 60 * 12; // 12 heure  //
    private final long refreshExpiration = 1000L * 60 * 60 * 24 * 7; // 7 jrs

    public String generateToken(String unix) {
        return Jwts.builder()
                .setSubject(unix)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(String unix) {
        return Jwts.builder()
                .setSubject(unix)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getKey())
                .compact();
    }

    public boolean isRefreshToken(String token) {
        String type = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type", String.class);

        return "refresh".equals(type);
    }

    public String extractUnix(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractType(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}
