package com.example.oauth2_demo.service;

import com.example.oauth2_demo.model.User;
import com.example.oauth2_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId();

        String email   = oAuth2User.getAttribute("email");
        String name    = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // GitHub spécifique
        if ("github".equals(provider)) {
            if (email == null) {
                email = oAuth2User.getAttribute("login") + "@github.com";
            }
            if (name == null) {
                name = oAuth2User.getAttribute("login");
            }
            picture = oAuth2User.getAttribute("avatar_url");
        }

        final String finalEmail   = email;
        final String finalName    = name;
        final String finalPicture = picture;

        userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setName(finalName);
            newUser.setPicture(finalPicture);
            newUser.setRole("USER");
            return userRepository.save(newUser);
        });

        return oAuth2User;
    }
}
