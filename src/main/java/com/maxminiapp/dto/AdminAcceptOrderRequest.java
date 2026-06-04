package com.maxminiapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AdminAcceptOrderRequest {

    @NotBlank
    private String eta;

    @NotNull
    private LocalDateTime etaAt;

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public LocalDateTime getEtaAt() {
        return etaAt;
    }

    public void setEtaAt(LocalDateTime etaAt) {
        this.etaAt = etaAt;
    }
}
