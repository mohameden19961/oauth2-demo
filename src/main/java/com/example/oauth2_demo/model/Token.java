package com.example.oauth2_demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BUG FIX: JWT peut dépasser 512 chars → utiliser TEXT
    @Column(columnDefinition = "TEXT")
    private String token;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean valid;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
