package com.hlala.service;

import com.hlala.domain.Enums;

import java.util.Set;
import java.util.UUID;

/**
 * Who is making a call. Resolved once per request by the API layer and handed
 * to services, so services never reach into the security context themselves.
 */
public record Actor(UUID userId, Set<Enums.Role> roles) {

    public boolean has(Enums.Role role) {
        return roles != null && roles.contains(role);
    }
}
