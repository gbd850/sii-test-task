package com.test.sii.dto;

import com.test.sii.model.Product;

import java.math.BigDecimal;
import java.sql.Date;

public record PurchaseResponse(

        BigDecimal regularPrice,
        BigDecimal discountAmount,
        Date date,
        String promoCode,
        ProductResponse product
) {
}
