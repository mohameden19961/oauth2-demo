package com.example.oauth2_demo.scheduled;

import com.example.oauth2_demo.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenCleanupTask {

    @Autowired
    private TokenRepository tokenRepository;

    // BUG FIX: utilise une requête batch au lieu de findAll() + N saves
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void invalidateExpiredTokens() {
        int count = tokenRepository.invalidateExpiredTokens(java.time.LocalDateTime.now());
        if (count > 0) {
            System.out.println(count + " token(s) expiré(s) invalidé(s) en base.");
        }
    }
}
