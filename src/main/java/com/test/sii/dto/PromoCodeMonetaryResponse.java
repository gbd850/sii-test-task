package com.test.sii.dto;

import java.math.BigDecimal;
import java.sql.Date;

public record PromoCodeMonetaryResponse(

        String code,
        Date expirationDate,
        BigDecimal amount,
        String currency
) {
}
