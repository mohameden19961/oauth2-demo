package com.example.oauth2_demo.config;

import com.example.oauth2_demo.model.Token;
import com.example.oauth2_demo.model.User;
import com.example.oauth2_demo.repository.TokenRepository;
import com.example.oauth2_demo.repository.UserRepository;
import com.example.oauth2_demo.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private TokenRepository tokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email   = oAuth2User.getAttribute("email");
        String name    = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        final String finalEmail   = email;
        final String finalName    = name;
        final String finalPicture = picture;

        // Créer ou récupérer l'utilisateur
        User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setName(finalName);
            newUser.setPicture(finalPicture);
            newUser.setRole("USER");
            return userRepository.save(newUser);
        });

        // Révoquer l'ancien token
        tokenRepository.deleteByEmail(finalEmail);

        // Générer les nouveaux tokens
        String accessToken  = jwtService.generateToken(finalEmail, user.getRole());
        String refreshToken = jwtService.generateRefreshToken(finalEmail);

        // Sauvegarder en base
        Token token = new Token();
        token.setToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setEmail(finalEmail);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        token.setValid(true);
        tokenRepository.save(token);

        // Poser le cookie JWT (HttpOnly)
        Cookie jwtCookie = new Cookie("jwt", accessToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 15);
        response.addCookie(jwtCookie);

        // Lire le redirect_uri depuis le cookie (posé par /auth/login)
        String redirectUri = null;
        if (request.getCookies() != null) {
            redirectUri = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("redirect_uri"))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
        }

        if (redirectUri != null && !redirectUri.isBlank()) {
            // Supprimer le cookie redirect_uri
            Cookie clearCookie = new Cookie("redirect_uri", "");
            clearCookie.setMaxAge(0);
            clearCookie.setPath("/");
            response.addCookie(clearCookie);

            // Rediriger vers l'app cliente avec les tokens en query params
            String separator = redirectUri.contains("?") ? "&" : "?";
            response.sendRedirect(redirectUri + separator
                + "access_token=" + accessToken
                + "&refresh_token=" + refreshToken
                + "&token_type=Bearer"
                + "&expires_in=900");
        } else {
            // Pas de redirect_uri → comportement par défaut (JSON)
            response.sendRedirect("/api/auth/token");
        }
    }
}
