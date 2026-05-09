package com.maxminiapp.dto;

import java.math.BigDecimal;

public record PaymentDetailsResponse(
        String paymentDetails,
        BigDecimal cityDeliveryFee
) {
}
