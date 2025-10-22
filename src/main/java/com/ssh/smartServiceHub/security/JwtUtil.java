package com.ssh.smartServiceHub.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtxpirationMs;

    private Key key() {
        byte[] bytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(Arrays.copyOf(bytes, Math.max(32, bytes.length)));
    }
}
