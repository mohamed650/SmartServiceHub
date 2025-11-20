package com.ssh.smartServiceHub.security;

import com.ssh.smartServiceHub.entity.User;
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

    @Value("${app.jwt.refreshExpirationMs}")
    private long jwtRefreshExpirationMs;

    private Key key() {
        byte[] bytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(Arrays.copyOf(bytes, Math.max(32, bytes.length)));
    }

    private Key refreshKey() {
        byte[] bytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(Arrays.copyOf(bytes, Math.max(32, bytes.length)));
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);

        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtxpirationMs);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("email", user.getEmail())
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(refreshKey(), SignatureAlgorithm.HS256)
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

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(refreshKey()).build().parseClaimsJws(token);
            return true;
        } catch(JwtException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public  String getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(refreshKey()).build().parseClaimsJws(token).getBody();
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

    public Date getExpirationDateFromRefreshToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(refreshKey()).build().parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }

    public long getAccessTokenExpiryMs() {
        return jwtxpirationMs;
    }
}
