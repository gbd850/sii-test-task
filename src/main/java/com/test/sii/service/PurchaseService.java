package com.test.sii.service;

import com.test.sii.dto.ProductResponse;
import com.test.sii.dto.PurchaseDiscountResponse;
import com.test.sii.dto.PurchaseRequest;
import com.test.sii.dto.PurchaseResponse;
import com.test.sii.model.Product;
import com.test.sii.model.PromoCode;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.model.Purchase;
import com.test.sii.repository.ProductRepository;
import com.test.sii.repository.PromoCodeMonetaryRepository;
import com.test.sii.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final PromoCodeMonetaryRepository promoCodeMonetaryRepository;

    private PromoCodeMonetary getPromoCodeMonetary(String promoCode) {
        return promoCodeMonetaryRepository.findByCode(promoCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found", new Exception("Promo code " + promoCode + " was not found")));
    }

    private Product getProduct(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found", new Exception("Product with id " + productId + " was not found")));
    }

    private PurchaseDiscountResponse validatePromoCodeWithProduct(PromoCode promoCode, Product product) {
        if (promoCode.isExpired()) {
            return new PurchaseDiscountResponse(product.getPrice(), product.getCurrency().getCurrency(), "Promo code has expired");
        }
        if (!promoCode.getCurrency().equals(product.getCurrency())) {
            return new PurchaseDiscountResponse(product.getPrice(), product.getCurrency().getCurrency(), "Currencies of promo code and product don't match");
        }
        return null;
    }

    public PurchaseDiscountResponse getDiscountPrice(Integer productId, String promoCode) {
        Product product = getProduct(productId);

        PromoCodeMonetary promoCodeMonetary = getPromoCodeMonetary(promoCode);

        PurchaseDiscountResponse error = validatePromoCodeWithProduct(promoCodeMonetary, product);

        if (error != null) {
            return error;
        }

        BigDecimal discountPrice = promoCodeMonetary.calculateDiscountPrice(product);

        return new PurchaseDiscountResponse(discountPrice, product.getCurrency().getCurrency(), null);
    }


    public PurchaseResponse createPurchase(PurchaseRequest purchaseRequest) {
        Product product = getProduct(purchaseRequest.productId());

        PromoCodeMonetary promoCodeMonetary = getPromoCodeMonetary(purchaseRequest.promoCode());

        Purchase purchase = new Purchase();

        purchase.setDate(new Date(Date.from(Instant.now()).getTime()));
        purchase.setRegularPrice(product.getPrice());
        purchase.setDiscountAmount(promoCodeMonetary.getAmount());
        purchase.setPromoCode(promoCodeMonetary);
        purchase.setProduct(product);

        try {
            purchaseRepository.save(purchase);
            promoCodeMonetary.use();
            promoCodeMonetaryRepository.save(promoCodeMonetary);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        ProductResponse productResponse = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );

        return new PurchaseResponse(
                purchase.getRegularPrice(),
                purchase.getDiscountAmount(),
                purchase.getDate(),
                purchase.getPromoCode().getCode(),
                productResponse
        );
    }
}
