package lk.busroute.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check and basic info endpoint.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final Clock clock;
    private final DataSource dataSource;

    @Autowired
    public HealthController(Clock clock, DataSource dataSource) {
        this.clock = clock;
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "Sri Lanka Bus Route Journey Planner");
        response.put("version", "0.1.0");
        response.put("timestamp", ZonedDateTime.now(clock).toString());

        // Check database connectivity
        try (Connection conn = dataSource.getConnection()) {
            response.put("database", "CONNECTED");
        } catch (Exception e) {
            response.put("database", "DISCONNECTED");
            response.put("databaseError", e.getMessage());
        }

        return response;
    }
}
