package com.phitrading.exchange.config;

import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds a default ADMIN user on startup if none exists.
 * This project currently does not model roles in the DB; the JpaUserDetailsService
 * grants ROLE_ADMIN to the username "admin". This runner ensures that user exists.
 */
@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapRunner(UserAccountRepository userAccountRepository,
                                PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        final String adminUsername = "ADMIN";
        final String adminEmail = "admin@phi-trading.local";
        final String rawPassword = "admin123";

        boolean exists = userAccountRepository.findByUsername(adminUsername).isPresent();
        if (exists) {
            log.info("Admin user already exists, skipping bootstrap");
            return;
        }

        UserAccount admin = new UserAccount();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setCashBalance(new BigDecimal("100000.00"));
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(admin);
        log.info("Default admin user created: username={}, password={}", adminUsername, rawPassword);
    }
}
