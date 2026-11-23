package com.hlala.schemas;

import com.hlala.domain.Enums;
import com.hlala.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public final class AccountSchemas {

    private AccountSchemas() {
    }

    public record RegisterRequest(@NotBlank String phone, @NotBlank String name) {
    }

    public record RoleRequest(@NotNull Enums.Role role) {
    }

    public record GrantRoleRequest(@NotNull UUID userId, @NotNull Enums.Role role) {
    }

    /** Phone is included only on the caller's own account view, never on views of other people. */
    public record AccountDto(UUID id, String name, String phone, boolean phoneVerified, Set<Enums.Role> roles) {
        public static AccountDto of(User u, Set<Enums.Role> roles) {
            return new AccountDto(u.getId(), u.getName(), u.getPhone(), u.isPhoneVerified(), roles);
        }
    }
}
