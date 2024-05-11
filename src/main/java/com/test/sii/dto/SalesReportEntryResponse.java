package com.test.sii.dto;

import java.math.BigDecimal;

public record SalesReportEntryResponse(
        String currency,
        BigDecimal totalAmount,
        BigDecimal totalDiscount,
        Long numberOfPurchases
) {}
