package com.example.oauth2_demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    // BUG FIX: clé lue depuis application.properties, plus codée en dur
    private final SecretKey SECRET_KEY;

    // Access token : expire après 15 minutes
    private static final long ACCESS_EXPIRATION  = 1000L * 60 * 15;
    // Refresh token : expire après 7 jours
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Générer Access Token
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Générer Refresh Token
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("type", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
