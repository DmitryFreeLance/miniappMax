package com.maxminiapp.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        Long maxUserId,
        boolean admin,
        String fullName,
        String phone,
        String address,
        LocalDateTime createdAt
) {
}
