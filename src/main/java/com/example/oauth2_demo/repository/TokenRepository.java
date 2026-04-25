package com.example.oauth2_demo.repository;

import com.example.oauth2_demo.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenAndValidTrue(String token);
    Optional<Token> findByRefreshTokenAndValidTrue(String refreshToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.email = :email")
    void deleteByEmail(String email);

    // BUG FIX: invalide en masse les tokens expirés sans charger toute la table
    @Modifying
    @Query("UPDATE Token t SET t.valid = false WHERE t.valid = true AND t.expiresAt < :now")
    int invalidateExpiredTokens(java.time.LocalDateTime now);
}
