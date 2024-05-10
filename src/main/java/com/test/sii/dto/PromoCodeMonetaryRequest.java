package com.test.sii.dto;

import com.test.sii.util.PromoCodePattern;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Locale;

public record PromoCodeMonetaryRequest(

        @PromoCodePattern
        String code,
        Date expirationDate,
        int maxUsages,
        BigDecimal amount,
        String currency
) {
    public PromoCodeMonetaryRequest {
        currency = currency.toUpperCase(Locale.ROOT);
    }
}
