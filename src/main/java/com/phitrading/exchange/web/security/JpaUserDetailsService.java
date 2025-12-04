package com.phitrading.exchange.web.security;

import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(JpaUserDetailsService.class);

    private final UserAccountRepository userAccountRepository;

    public JpaUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // For login we treat the provided principal as an email
        UserAccount ua = userAccountRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        log.info("Loaded user for authentication: {}", usernameOrEmail);

        String role = ua.getRole() != null ? ua.getRole() : "USER";

        return org.springframework.security.core.userdetails.User
                .withUsername(ua.getEmail())
                .password(ua.getPasswordHash())
                .authorities("ROLE_" + role)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
