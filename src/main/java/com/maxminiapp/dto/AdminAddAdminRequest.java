package com.maxminiapp.dto;

import jakarta.validation.constraints.NotNull;

public class AdminAddAdminRequest {

    @NotNull
    private Long maxUserId;

    public Long getMaxUserId() {
        return maxUserId;
    }

    public void setMaxUserId(Long maxUserId) {
        this.maxUserId = maxUserId;
    }
}
