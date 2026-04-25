package com.example.oauth2_demo.controller;

import com.example.oauth2_demo.model.Token;
import com.example.oauth2_demo.model.User;
import com.example.oauth2_demo.repository.TokenRepository;
import com.example.oauth2_demo.repository.UserRepository;
import com.example.oauth2_demo.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private TokenRepository tokenRepository;

    @GetMapping("/token")
    public Map<String, Object> generateToken(
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletResponse response) {

        String email   = principal.getAttribute("email");
        String name    = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPicture(picture);
            newUser.setRole("USER");
            return userRepository.save(newUser);
        });

        tokenRepository.deleteByEmail(email);

        String accessToken  = jwtService.generateToken(email, user.getRole());
        String refreshToken = jwtService.generateRefreshToken(email);

        Token token = new Token();
        token.setToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setEmail(email);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        token.setValid(true);
        tokenRepository.save(token);

        Cookie cookie = new Cookie("jwt", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 15);
        response.addCookie(cookie);

        return Map.of(
            "access_token", accessToken,
            "refresh_token", refreshToken,
            "token_type", "Bearer",
            "expires_in", "15 minutes",
            "user", Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
            )
        );
    }

    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = null;
        if (request.getCookies() != null) {
            refreshToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("refresh_token"))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
        }

        if (refreshToken == null) {
            String header = request.getHeader("X-Refresh-Token");
            if (header != null) refreshToken = header;
        }

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            return Map.of("error", "Refresh token invalide ou expiré");
        }

        // BUG FIX: vérifier que c'est bien un refresh token (pas un access token)
        if (!"refresh".equals(jwtService.extractType(refreshToken))) {
            return Map.of("error", "Token fourni n'est pas un refresh token");
        }

        Token savedToken = tokenRepository
            .findByRefreshTokenAndValidTrue(refreshToken)
            .orElse(null);

        if (savedToken == null) {
            return Map.of("error", "Refresh token non trouvé en BDD");
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow();

        String newAccessToken = jwtService.generateToken(email, user.getRole());

        savedToken.setToken(newAccessToken);
        savedToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(savedToken);

        Cookie cookie = new Cookie("jwt", newAccessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 15);
        response.addCookie(cookie);

        return Map.of(
            "access_token", newAccessToken,
            "token_type", "Bearer",
            "expires_in", "15 minutes"
        );
    }

    // ✅ LOGOUT — invalide le token en BDD
    @PostMapping("/logout")
    public Map<String, Object> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String jwt = null;
        if (request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("jwt"))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
        }

        if (jwt != null) {
            tokenRepository.findByTokenAndValidTrue(jwt).ifPresent(t -> {
                t.setValid(false);
                tokenRepository.save(t);
            });
        }

        // Supprimer le cookie
        Cookie cookie = new Cookie("jwt", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        return Map.of("message", "Déconnecté avec succès ✅");
    }

    // ✅ VALIDATE — pour les autres backends : vérifier si un token est valide
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {

        // Chercher le token dans le cookie ou le header
        String jwt = null;
        if (request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("jwt"))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
        }
        if (jwt == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                jwt = header.substring(7);
            }
        }

        if (jwt == null) {
            return ResponseEntity.status(401)
                .body(Map.of("valid", false, "error", "Aucun token fourni"));
        }

        if (!jwtService.isTokenValid(jwt)) {
            return ResponseEntity.status(401)
                .body(Map.of("valid", false, "error", "Token invalide ou expiré"));
        }

        if (!"access".equals(jwtService.extractType(jwt))) {
            return ResponseEntity.status(401)
                .body(Map.of("valid", false, "error", "Ce n'est pas un access token"));
        }

        boolean existsInDb = tokenRepository.findByTokenAndValidTrue(jwt).isPresent();
        if (!existsInDb) {
            return ResponseEntity.status(401)
                .body(Map.of("valid", false, "error", "Token révoqué ou expiré en base"));
        }

        String email = jwtService.extractEmail(jwt);
        String role  = jwtService.extractRole(jwt);

        // Retourner les infos utilisateur pour que l'autre backend puisse les utiliser
        return ResponseEntity.ok(Map.of(
            "valid", true,
            "email", email,
            "role", role
        ));
    }
}
