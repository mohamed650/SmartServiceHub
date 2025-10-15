package com.ssh.smartServiceHub.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("smartServiceHub")
public class SmartServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return null;
    }
}
