package com.example.oauth2_demo.controller;

import com.example.oauth2_demo.model.User;
import com.example.oauth2_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public Map<String, Object> getMe(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
            .map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("name", user.getName());
                map.put("email", user.getEmail());
                map.put("role", user.getRole());
                return map;
            })
            .orElse(Map.of("error", "User not found"));
    }
}
