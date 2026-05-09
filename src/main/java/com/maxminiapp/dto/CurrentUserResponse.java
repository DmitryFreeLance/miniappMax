package com.maxminiapp.dto;

public record CurrentUserResponse(
        boolean authenticated,
        Long maxUserId,
        boolean admin,
        String fullName,
        String phone
) {
}
