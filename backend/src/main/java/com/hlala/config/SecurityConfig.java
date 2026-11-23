package com.hlala.config;

import com.hlala.repository.UserRepository;
import com.hlala.service.ProfileLookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Two layers of access control:
 *  1. Role checks: @PreAuthorize on each controller method (e.g. only ADMIN
 *     reaches dispute resolution and payout approval).
 *  2. Ownership checks: inside the services via ProfileLookup (a landlord can
 *     only touch THEIR rooms), so they hold even if an annotation is missed.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${hlala.dev-auth.enabled:false}")
    private boolean devAuthEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository users, ProfileLookup lookup)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // stateless token API, no cookies
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 console in local
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Explicit Ant matchers: with the H2 console there are two servlets,
                        // and Spring Security rejects bare string patterns in that setup.
                        .requestMatchers(antMatcher(HttpMethod.POST, "/accounts")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/listings")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/listings/*")).permitAll()
                        .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(antMatcher("/error")).permitAll()
                        .anyRequest().authenticated());

        if (devAuthEnabled) {
            http.addFilterBefore(new DevHeaderAuthenticationFilter(users, lookup),
                    UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }
}
