package com.test.sii.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Locale;

public record ProductCreateRequest(
        @NotEmpty
        String name,
        String description,
        @NotNull
        BigDecimal price,
        @NotNull
        String currency
) {
    public ProductCreateRequest {
        currency = currency.toUpperCase(Locale.ROOT);
    }
}