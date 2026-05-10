package com.maxminiapp.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminAcceptOrderRequest {

    @NotBlank
    private String eta;

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }
}
