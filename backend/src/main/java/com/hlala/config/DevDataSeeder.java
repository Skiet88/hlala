package com.hlala.config;

import com.hlala.domain.User;
import com.hlala.factory.ProfileFactory;
import com.hlala.factory.UserFactory;
import com.hlala.repository.AdminProfileRepository;
import com.hlala.repository.CheckerProfileRepository;
import com.hlala.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * LOCAL ONLY. Admin and checker roles can only be granted by an admin, so
 * someone has to exist first. Seeds two admins (the payout rule needs two)
 * and one checker, and logs their ids for use as X-Dev-User-Id.
 */
@Component
@Profile("local")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository users;
    private final AdminProfileRepository admins;
    private final CheckerProfileRepository checkers;
    private final UserFactory userFactory;
    private final ProfileFactory profileFactory;

    public DevDataSeeder(UserRepository users, AdminProfileRepository admins, CheckerProfileRepository checkers,
                         UserFactory userFactory, ProfileFactory profileFactory) {
        this.users = users;
        this.admins = admins;
        this.checkers = checkers;
        this.userFactory = userFactory;
        this.profileFactory = profileFactory;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (admins.count() > 0) {
            return;
        }
        User adminA = users.save(userFactory.create("+27800000001", "Dev Admin A"));
        User adminB = users.save(userFactory.create("+27800000002", "Dev Admin B"));
        User checker = users.save(userFactory.create("+27800000003", "Dev Checker"));
        admins.save(profileFactory.admin(adminA.getId()));
        admins.save(profileFactory.admin(adminB.getId()));
        checkers.save(profileFactory.checker(checker.getId()));

        log.info("Dev users seeded. Use as X-Dev-User-Id:");
        log.info("  admin A  {}", adminA.getId());
        log.info("  admin B  {}", adminB.getId());
        log.info("  checker  {}", checker.getId());
    }
}
