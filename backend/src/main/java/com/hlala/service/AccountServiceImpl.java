package com.hlala.service;

import com.hlala.domain.Enums;
import com.hlala.domain.User;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.ProfileFactory;
import com.hlala.factory.UserFactory;
import com.hlala.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository users;
    private final TenantProfileRepository tenants;
    private final LandlordProfileRepository landlords;
    private final CheckerProfileRepository checkers;
    private final AdminProfileRepository admins;
    private final UserFactory userFactory;
    private final ProfileFactory profileFactory;
    private final ProfileLookup lookup;

    public AccountServiceImpl(UserRepository users, TenantProfileRepository tenants,
                              LandlordProfileRepository landlords, CheckerProfileRepository checkers,
                              AdminProfileRepository admins, UserFactory userFactory,
                              ProfileFactory profileFactory, ProfileLookup lookup) {
        this.users = users;
        this.tenants = tenants;
        this.landlords = landlords;
        this.checkers = checkers;
        this.admins = admins;
        this.userFactory = userFactory;
        this.profileFactory = profileFactory;
        this.lookup = lookup;
    }

    @Override
    @Transactional
    public User register(String phone, String name) {
        if (phone != null && users.findByPhone(phone).isPresent()) {
            throw new ValidationException("That phone number already has an account");
        }
        return users.save(userFactory.create(phone, name));
    }

    @Override
    @Transactional
    public void takeRole(Actor actor, Enums.Role role) {
        if (role != Enums.Role.TENANT && role != Enums.Role.LANDLORD) {
            throw new ForbiddenOperationException("Only tenant or landlord can be chosen; other roles are granted by an admin");
        }
        addProfile(actor.userId(), role);
    }

    @Override
    @Transactional
    public void grantRole(Actor admin, UUID userId, Enums.Role role) {
        lookup.admin(admin);
        get(userId);
        addProfile(userId, role);
    }

    @Override
    @Transactional(readOnly = true)
    public User get(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void addProfile(UUID userId, Enums.Role role) {
        if (lookup.rolesOf(userId).contains(role)) {
            return; // idempotent: already has it
        }
        switch (role) {
            case TENANT -> tenants.save(profileFactory.tenant(userId));
            case LANDLORD -> landlords.save(profileFactory.landlord(userId));
            case CHECKER -> checkers.save(profileFactory.checker(userId));
            case ADMIN -> admins.save(profileFactory.admin(userId));
        }
    }
}
