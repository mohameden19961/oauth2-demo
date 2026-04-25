package com.example.oauth2_demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * Point d'entrée pour les applications clientes.
     * Exemple: http://localhost:8080/auth/login?redirect_uri=http://mon-app.com/callback
     */
    @GetMapping("/auth/login")
    public String login(@RequestParam(value = "redirect_uri", required = false) String redirectUri, 
                        HttpServletResponse response) {
        
        if (redirectUri != null && !redirectUri.isBlank()) {
            Cookie cookie = new Cookie("redirect_uri", redirectUri);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(300); // 5 minutes pour se connecter
            response.addCookie(cookie);
        }

        // Redirige vers le flux Spring Security OAuth2 standard
        return "redirect:/oauth2/authorization/google";
    }
}
