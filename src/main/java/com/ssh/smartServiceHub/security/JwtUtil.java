package com.ssh.smartServiceHub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

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

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);

        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtxpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        Object rolesObj = claims.get("roles");
        if(rolesObj == null) Collections.emptyList();
        String rolesStr = rolesObj.toString();
        if(rolesStr == null) Collections.emptyList();
        return Arrays.asList(rolesStr.split(","));
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }
}
