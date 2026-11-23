package com.hlala.config;

import com.hlala.domain.Enums;
import com.hlala.repository.UserRepository;
import com.hlala.service.ProfileLookup;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * LOCAL DEVELOPMENT ONLY. Trusts an "X-Dev-User-Id" header as the caller's
 * identity so the API can be exercised before real sign-in exists.
 *
 * It is only added to the filter chain when hlala.dev-auth.enabled=true,
 * which is set in the local profile alone. In prod nothing authenticates
 * yet, so every protected endpoint answers 401 until phone OTP + tokens
 * are built - failing closed, never open.
 */
public class DevHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Dev-User-Id";

    private final UserRepository users;
    private final ProfileLookup lookup;

    public DevHeaderAuthenticationFilter(UserRepository users, ProfileLookup lookup) {
        this.users = users;
        this.lookup = lookup;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header != null && !header.isBlank()) {
            try {
                UUID userId = UUID.fromString(header.trim());
                if (users.existsById(userId)) {
                    List<SimpleGrantedAuthority> authorities = lookup.rolesOf(userId).stream()
                            .map(Enums.Role::name)
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .toList();
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(userId, null, authorities));
                }
            } catch (IllegalArgumentException ignored) {
                // malformed id: stay anonymous
            }
        }
        chain.doFilter(request, response);
    }
}
