package com.maxminiapp.integration;

public record PaymentCreationResult(
        String id,
        String status,
        String confirmationUrl
) {
}
