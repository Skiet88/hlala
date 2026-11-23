package com.hlala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * UserDetailsServiceAutoConfiguration is excluded because Hlala has no
 * username/password login: identity comes from the auth filter. Left on,
 * Spring creates an unused in-memory user and prints its password at startup.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class HlalaApplication {
    public static void main(String[] args) {
        SpringApplication.run(HlalaApplication.class, args);
    }
}
