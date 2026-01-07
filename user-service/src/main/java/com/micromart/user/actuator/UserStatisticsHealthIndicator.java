package com.micromart.user.actuator;

import com.micromart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for User Statistics.
 * <p>
 * Spring Actuator: Custom HealthIndicator
 * <p>
 * Provides user-related metrics in the health endpoint.
 */
@Component
@RequiredArgsConstructor
public class UserStatisticsHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    @Override
    public Health health() {
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countActiveUsers();
            long inactiveUsers = totalUsers - activeUsers;

            return Health.up()
                    .withDetail("status", "OK")
                    .withDetail("totalUsers", totalUsers)
                    .withDetail("activeUsers", activeUsers)
                    .withDetail("inactiveUsers", inactiveUsers)
                    .withDetail("message", "User service is healthy")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "ERROR")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
