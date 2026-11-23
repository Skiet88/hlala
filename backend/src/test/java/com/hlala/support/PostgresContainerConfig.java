package com.hlala.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * A throwaway Postgres for Spring tests, the same major version as
 * docker-compose.yml. Flyway runs the real migrations against it, so every
 * test run also proves the migrations and the entity mappings agree.
 * Needs Docker on the machine running the tests.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }
}
