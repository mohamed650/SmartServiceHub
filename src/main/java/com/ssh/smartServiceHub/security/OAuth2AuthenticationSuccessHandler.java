package com.ssh.smartServiceHub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssh.smartServiceHub.dto.LoginResponse;
import com.ssh.smartServiceHub.dto.UserDTO;
import com.ssh.smartServiceHub.entity.RefreshToken;
import com.ssh.smartServiceHub.service.RefreshTokenService;
import com.ssh.smartServiceHub.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        UserDTO userDTO = userService.findByUsername(email);

        String roleConstruct = userDTO.getRole();
        String role = (roleConstruct == null || roleConstruct.isBlank()) ? "ROLE_USER" :
                (roleConstruct.startsWith("ROLE_") ? roleConstruct : "ROLE_" + roleConstruct);

        var authority = new SimpleGrantedAuthority(role);
        var userDetails = new User(userDTO.getEmail(), "", Collections.singletonList(authority));

        String accessToken = jwtUtil.generateAccessToken(userDetails);

        var userEntity = new com.ssh.smartServiceHub.entity.User();
        userEntity.setId(userDTO.getId());
        userEntity.setEmail(userDTO.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userEntity);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var logResp = new LoginResponse(accessToken, refreshToken.getToken(), jwtUtil.getAccessTokenExpiryMs(), "Login via Google successful");
        response.getWriter().write(mapper.writeValueAsString(logResp));
    }
}
