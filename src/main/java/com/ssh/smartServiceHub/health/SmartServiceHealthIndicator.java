package com.ssh.smartServiceHub.health;

import com.ssh.smartServiceHub.repository.CategoryRepository;
import com.ssh.smartServiceHub.repository.ServiceRequestRepository;
import com.ssh.smartServiceHub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component("smartServiceHub")
public class SmartServiceHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public SmartServiceHealthIndicator(
            DataSource dataSource,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ServiceRequestRepository serviceRequestRepository
    ) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public Health health() {
        try {
            // Check database connectivity
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            long userCount = userRepository.count();
            long categoryCount = categoryRepository.count();
            long serviceRequestCount = serviceRequestRepository.count();

            return Health.up()
                    .withDetail("Database", "Connected")
                    .withDetail("users_count", userCount)
                    .withDetail("category_count", categoryCount)
                    .withDetail("service_request_count", serviceRequestCount)
                    .withDetail("status", "SmartServiceHub Running Smoothly")
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("database", "Disconnected or Error")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
