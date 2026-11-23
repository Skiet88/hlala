package com.hlala.service;

import com.hlala.domain.Enums;
import com.hlala.domain.User;

import java.util.UUID;

public interface AccountService {

    User register(String phone, String name);

    /** Self-service: a user may take on TENANT or LANDLORD. */
    void takeRole(Actor actor, Enums.Role role);

    /** Admin-only: CHECKER and ADMIN are granted, never self-assigned. */
    void grantRole(Actor admin, UUID userId, Enums.Role role);

    User get(UUID userId);
}
