package dev.solaris.memory.web;

import dev.solaris.memory.dto.HealthResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public HealthResponse health() {
        Map<String, String> services = new LinkedHashMap<>();
        services.put("database", checkDatabase());
        // M1 has no embeddings/LLM/VPN wiring yet (doc section 6/7); these
        // will report real status from M3/M2 onward.
        services.put("model", "not_configured");
        services.put("vpn", "not_applicable_server_side");

        boolean allOk = services.values().stream().allMatch(v -> v.equals("up") || v.startsWith("not_"));
        return new HealthResponse(allOk ? "ok" : "degraded", services);
    }

    private String checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "up";
        } catch (Exception e) {
            return "down";
        }
    }
}
