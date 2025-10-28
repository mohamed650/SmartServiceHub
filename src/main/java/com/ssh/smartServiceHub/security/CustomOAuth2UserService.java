package com.ssh.smartServiceHub.security;

import com.ssh.smartServiceHub.dto.UserDTO;
import com.ssh.smartServiceHub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        String email = (String) attributes.get("email");
        if(email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not returned by provider");
        }

        final UserDTO userDTO;
        try {
            userDTO = userService.findByUsername(email);
        } catch (RuntimeException ex) {
            throw new OAuth2AuthenticationException("User with email not registered in the system");
        }

        String roleConstruct = userDTO.getRole();
        String role = (roleConstruct == null || roleConstruct.isBlank()) ? "ROLE_USER" :
                (roleConstruct.startsWith("ROLE_") ? roleConstruct : "ROLE_" + roleConstruct);

        var authorities = Collections.singleton(new SimpleGrantedAuthority(role));

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        if(nameAttributeKey == null) nameAttributeKey = "sub";

        attributes.put("localUserRole", userDTO.getRole());
        attributes.put("localUserEmail", userDTO.getEmail());

        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }
}
