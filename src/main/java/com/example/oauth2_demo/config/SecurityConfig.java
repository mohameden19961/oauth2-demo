package com.example.oauth2_demo.config;

import com.example.oauth2_demo.service.OAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private OAuth2UserService oAuth2UserService;
    @Autowired private JwtFilter jwtFilter;
    @Autowired private OAuth2SuccessHandler oAuth2SuccessHandler;
    @Autowired private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Routes publiques
                .requestMatchers("/", "/public/**").permitAll()
                // Auth : login initié par le client, validate pour les autres backends
                .requestMatchers("/auth/login", "/api/auth/**").permitAll()
                // Admin uniquement
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                )
                // Utilise notre handler personnalisé au lieu de defaultSuccessUrl
                .successHandler(oAuth2SuccessHandler)
                .failureUrl("/login?error")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
