package dev.solaris.memory.support;

import dev.solaris.memory.dto.LoginRequest;
import dev.solaris.memory.dto.LoginResponse;
import dev.solaris.memory.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestClockConfig.class)
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
public abstract class AbstractIntegrationTest {

    protected static final String TEST_USERNAME = "admin";
    protected static final String TEST_PASSWORD = "admin";
    private static final Instant BASELINE_INSTANT = Instant.parse("2026-01-01T12:00:00Z");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("solaris_memory_test")
            .withUsername("solaris")
            .withPassword("solaris");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MutableClock mutableClock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthService authService;

    /**
     * Each test starts from a clean memories table and a known instant. The
     * revocation watermark (auth_state.min_issued_at) is re-synced to that same
     * instant too — it was originally set by Postgres' own {@code now()} at
     * migration time, which has nothing to do with this test clock.
     */
    @BeforeEach
    void resetTestState() {
        mutableClock.setInstant(BASELINE_INSTANT);
        jdbcTemplate.update("DELETE FROM memories");
        authService.revokeAllSessions();
    }

    protected String login(TestRestTemplate restTemplate) {
        LoginResponse response = restTemplate.postForObject(
                "/api/v1/auth/login", new LoginRequest(TEST_USERNAME, TEST_PASSWORD), LoginResponse.class);
        return response.token();
    }

    protected HttpEntity<Void> authEntity(TestRestTemplate restTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login(restTemplate));
        return new HttpEntity<>(headers);
    }

    protected <T> HttpEntity<T> authEntity(TestRestTemplate restTemplate, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login(restTemplate));
        return new HttpEntity<>(body, headers);
    }
}
