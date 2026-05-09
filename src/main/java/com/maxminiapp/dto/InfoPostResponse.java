package com.maxminiapp.dto;

import java.time.LocalDateTime;

public record InfoPostResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
