package com.example.oauth2_demo.config;

import com.example.oauth2_demo.repository.TokenRepository;
import com.example.oauth2_demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Ne pas filtrer ces chemins (OAuth2 les gère)
        return path.startsWith("/oauth2/")
            || path.startsWith("/login")
            || path.equals("/api/auth/token");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;

        // 1. Cherche dans le cookie
        if (request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("jwt"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        }

        // 2. Sinon cherche dans le header Authorization
        if (jwt == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        // 3. Valider le token
        if (jwt != null && jwtService.isTokenValid(jwt)) {

            // BUG FIX: rejeter les refresh tokens utilisés comme access tokens
            String tokenType = jwtService.extractType(jwt);
            if (!"access".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            boolean existsInDb = tokenRepository
                .findByTokenAndValidTrue(jwt)
                .isPresent();

            if (existsInDb) {
                String email = jwtService.extractEmail(jwt);
                String role  = jwtService.extractRole(jwt);

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        email, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
