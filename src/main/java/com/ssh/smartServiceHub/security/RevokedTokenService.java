package com.ssh.smartServiceHub.security;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RevokedTokenService {

    private final Map<String, Long> revoked = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::removeExpired, 1, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    void shutdown() {
        scheduledExecutorService.shutdown();
    }

    public void revokeToken(String token, Date expiresAt) {
        if(token == null || expiresAt == null) return;
        revoked.put(token, expiresAt.getTime());
    }

    public boolean isTokenRevoked(String token) {
        if(token == null) return false;
        Long exp = revoked.get(token);
        if(exp == null) return false;
        if(System.currentTimeMillis() > exp) {
            revoked.remove(token);
            return false;
        }
        return true;
    }

    private void removeExpired() {
        long now = System.currentTimeMillis();
        revoked.entrySet().removeIf(e -> e.getValue() <= now);
    }
}
