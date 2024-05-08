package com.test.sii.dto;

import com.test.sii.model.Currency;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        String name,
        String description,
        BigDecimal price,
        Currency currency
) {}
