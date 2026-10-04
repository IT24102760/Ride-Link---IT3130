package com.ridelink.accountservice.dto;

import com.ridelink.accountservice.model.Role;

// Returned after a successful login. accessToken is pasted into every service's Authorize box.
public record LoginResponse(String accessToken, String tokenType, long expiresIn,
                            String accountId, Role role) {
}
