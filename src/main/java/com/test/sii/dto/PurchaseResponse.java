package com.test.sii.dto;

import java.math.BigDecimal;
import java.sql.Date;

public record PurchaseResponse(

        BigDecimal regularPrice,
        BigDecimal discountAmount,
        DiscountMethod discountMethod,
        Date date,
        String promoCode,
        ProductResponse product,
        String warning
) {
}
