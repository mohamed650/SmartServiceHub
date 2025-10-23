package com.ssh.smartServiceHub.controller;

import com.ssh.smartServiceHub.dto.AuthResponse;
import com.ssh.smartServiceHub.dto.LoginRequest;
import com.ssh.smartServiceHub.dto.LoginResponse;
import com.ssh.smartServiceHub.dto.RegisterRequest;
import com.ssh.smartServiceHub.entity.User;
import com.ssh.smartServiceHub.mapper.UserMapper;
import com.ssh.smartServiceHub.repository.UserRepository;
import com.ssh.smartServiceHub.security.JwtUtil;
import com.ssh.smartServiceHub.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal);

        var userOpt = userService.findByUsername(loginRequest.getEmail());
        String msg = "Login successful";
        if (userOpt != null) {
            msg = "Login successful for user: " + loginRequest.getEmail();
        }

        return ResponseEntity.ok(new LoginResponse(token, msg));
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
}
