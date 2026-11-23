package com.hlala.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * A throwaway Postgres for the Spring test, started by Testcontainers in CI
 * (GitHub's runners have Docker). Flyway runs the real migrations against it,
 * so every CI run proves the migrations and the entity mappings agree without
 * touching the Supabase database. Nothing is needed locally.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }
}
