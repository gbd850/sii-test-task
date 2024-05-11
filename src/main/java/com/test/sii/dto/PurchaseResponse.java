package com.test.sii.dto;

import java.math.BigDecimal;
import java.sql.Date;

public record PurchaseResponse(

        BigDecimal regularPrice,
        BigDecimal discountAmount,
        Date date,
        ProductResponse product,
        String warning
) {
}
