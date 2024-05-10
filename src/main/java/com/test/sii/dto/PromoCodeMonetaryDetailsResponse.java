package com.test.sii.dto;

import java.math.BigDecimal;
import java.sql.Date;

public record PromoCodeMonetaryDetailsResponse(

        String code,
        Date expirationDate,
        int maxUsages,
        int usages,
        BigDecimal amount,
        String currency
) {
}
