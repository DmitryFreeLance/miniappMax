package com.maxminiapp.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminPaymentDetailsRequest {

    @NotBlank
    private String paymentDetails;

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
