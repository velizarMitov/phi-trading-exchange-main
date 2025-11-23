package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.web.dto.UpdateProfileRequest;
import com.phitrading.exchange.web.dto.UserProfileDto;

public interface ProfileService {
    UserProfileDto getProfile(String username);

    void updateProfile(String username, UpdateProfileRequest request);
}
