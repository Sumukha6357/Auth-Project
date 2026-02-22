package com.example.idp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "RUN_TESTCONTAINERS", matches = "true")
class FlywayOrderIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("idp")
        .withUsername("idp")
        .withPassword("idp");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("POSTGRES_URL", postgres::getJdbcUrl);
        registry.add("POSTGRES_USER", postgres::getUsername);
        registry.add("POSTGRES_PASSWORD", postgres::getPassword);
        registry.add("IDP_KEY_ENCRYPTION_SECRET", () -> "test-secret-key");
        registry.add("IDP_ADMIN_BOOTSTRAP_EMAIL", () -> "admin@example.com");
        registry.add("IDP_ADMIN_BOOTSTRAP_PASSWORD", () -> "Admin123!!");
        registry.add("IDP_FAIL_FAST_SECRETS", () -> "true");
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Environment environment;

    @Test
    void flywayRunsBeforeJpaValidationAndSchemaIsMigrationControlled() {
        Integer applied = jdbcTemplate.queryForObject(
            "select count(*) from flyway_schema_history where success = true", Integer.class);
        Integer tenantTableExists = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where table_name = 'tenants'", Integer.class);

        assertThat(applied).isNotNull().isGreaterThanOrEqualTo(2);
        assertThat(tenantTableExists).isEqualTo(1);
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }
}
