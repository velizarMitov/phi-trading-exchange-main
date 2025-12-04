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
        // Allow login with either username OR email
        UserAccount ua = userAccountRepository.findByUsername(usernameOrEmail)
                .or(() -> userAccountRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        log.info("Loaded user for authentication: {} (id={})", usernameOrEmail, ua.getId());

        String rawRole = ua.getRole() != null ? ua.getRole().trim() : "USER";

        var userBuilder = org.springframework.security.core.userdetails.User
                // expose principal name as username (not email) to keep UI consistent
                .withUsername(ua.getUsername())
                .password(ua.getPasswordHash())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false);

        // If role already prefixed with ROLE_, use authorities; otherwise, use roles() to add prefix safely
        if (rawRole.toUpperCase().startsWith("ROLE_")) {
            return userBuilder.authorities(rawRole.toUpperCase()).build();
        } else {
            return userBuilder.roles(rawRole.toUpperCase()).build();
        }
    }
}
