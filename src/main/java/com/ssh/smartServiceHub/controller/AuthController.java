package com.ssh.smartServiceHub.controller;

import com.ssh.smartServiceHub.dto.*;
import com.ssh.smartServiceHub.entity.RefreshToken;
import com.ssh.smartServiceHub.entity.User;
import com.ssh.smartServiceHub.mapper.UserMapper;
import com.ssh.smartServiceHub.repository.UserRepository;
import com.ssh.smartServiceHub.security.JwtUtil;
import com.ssh.smartServiceHub.security.RevokedTokenService;
import com.ssh.smartServiceHub.service.RefreshTokenService;
import com.ssh.smartServiceHub.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    private RevokedTokenService revokedTokenService;

    public AuthController(UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil, UserService userService,
                          RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(principal);

        User userOpt = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userOpt);
        String msg = "Login successful";
        if (userOpt != null) {
            msg = "Login successful for user: " + loginRequest.getEmail();
        }

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken.getToken(), jwtUtil.getAccessTokenExpiryMs(), msg));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse("Email already registered"));
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == null ? "USER" : request.getRole().toUpperCase());

        userRepository.save(user);
        return ResponseEntity.ok(new AuthResponse("User Registered"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();
        if(requestToken == null || !jwtUtil.validateRefreshToken(requestToken)) {
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid refresh token"));
        }

        java.util.Optional<RefreshToken> optionalRt = refreshTokenService.findByToken(requestToken);
        if (optionalRt.isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResponse("Refresh token not found"));
        }

        RefreshToken rt;
        try {
            rt = refreshTokenService.verifyExpiration(optionalRt.get());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new AuthResponse(ex.getMessage()));
        }

        User user = rt.getUser();
        System.out.println(user.getEmail());
        var userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(user.getRole() == null ? "ROLE_USER" : "ROLE_" + user.getRole())
                        .build();

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        RefreshToken newRt = refreshTokenService.createRefreshToken(user);

        String msg = "Token refreshed successfully!";
        LoginResponse response = new LoginResponse(newAccessToken, newRt.getToken(), jwtUtil.getAccessTokenExpiryMs(), msg);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader) {
        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")) {
            if(refreshHeader != null) {
                refreshTokenService.findByToken(refreshHeader)
                        .ifPresent(rt -> refreshTokenService.deleteByUserId(rt.getUser().getId()));
            }
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, 0, "No Bearer token in Authorization header"));
        }

        String token = header.substring(7);
        if(!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, null, 0, "Invalid Token"));
        }

        Date expiresAt = jwtUtil.getExpirationDateFromToken(token);
        revokedTokenService.revokeToken(token, expiresAt);

        if(refreshHeader != null) {
            refreshTokenService.findByToken(refreshHeader)
                    .ifPresent(rt -> refreshTokenService.deleteByUserId(rt.getUser().getId()));
        }

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new LoginResponse(null, null, 0, "Logged out successfully"));
    }
}
