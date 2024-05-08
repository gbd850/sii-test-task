package com.test.sii.dto;

import java.math.BigDecimal;
import java.util.Locale;

public record ProductRequest(
        String name,
        String description,
        BigDecimal price,
        String currency
) {
    public ProductRequest {
        currency = currency.toUpperCase(Locale.ROOT);
    }
}
