package com.test.sii.dto;

import com.test.sii.util.PromoCodePattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Locale;

public record PromoCodeRequest(

        @NotBlank
        @PromoCodePattern
        String code,
        @NotNull
        Date expirationDate,
        int maxUsages,
        @NotNull
        BigDecimal amount,
        @NotNull
        @NotBlank
        String currency
) {
    public PromoCodeRequest {
        currency = currency.toUpperCase(Locale.ROOT);
    }
}
