package com.phitrading.exchange.domain.service;

public interface RegistrationService {
    void registerNewUser(String username, String email, String rawPassword);
}
