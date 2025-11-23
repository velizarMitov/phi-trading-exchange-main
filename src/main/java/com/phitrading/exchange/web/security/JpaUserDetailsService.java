package com.phitrading.exchange.web.security;

import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(JpaUserDetailsService.class);

    private final UserAccountRepository userAccountRepository;

    public JpaUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount ua = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        log.info("Loaded user for authentication: {}", username);
        // Default authority: ROLE_USER. Grant ROLE_ADMIN to the seeded admin account.
        boolean isAdmin = "admin".equalsIgnoreCase(ua.getUsername());
        List<SimpleGrantedAuthority> authorities = isAdmin
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(
                ua.getUsername(),
                ua.getPasswordHash(),
                authorities
        );
    }
}
