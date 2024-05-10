package com.test.sii.dto;

import java.math.BigDecimal;

public record PurchaseDiscountResponse(
        BigDecimal discountPrice,
        String currency,
        String warning
) {}
