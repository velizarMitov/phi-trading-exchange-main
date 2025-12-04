package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.domain.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public RegistrationServiceImpl(UserAccountRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerNewUser(String username, String email, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        userRepo.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Username already taken");
        });

        UserAccount ua = new UserAccount();
        ua.setUsername(username.trim());
        ua.setEmail(email.trim());
        ua.setPasswordHash(passwordEncoder.encode(rawPassword));
        ua.setCashBalance(new BigDecimal("10000.00"));
        ua.setCreatedAt(LocalDateTime.now());
        ua.setUpdatedAt(LocalDateTime.now());

        userRepo.save(ua);
        log.info("Registered new user: {}", username);
    }
}
