package com.test.sii.controller;

import com.test.sii.dto.PurchaseDiscountResponse;
import com.test.sii.dto.PurchaseRequest;
import com.test.sii.dto.PurchaseResponse;
import com.test.sii.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("discount")
    public ResponseEntity<PurchaseDiscountResponse> getDiscountPrice(@RequestParam("productId") Integer productId, @RequestParam("promoCode") String promoCode) {
        return new ResponseEntity<>(purchaseService.getDiscountPrice(productId, promoCode), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@RequestBody PurchaseRequest purchaseRequest) {
        return new ResponseEntity<>(purchaseService.createPurchase(purchaseRequest), HttpStatus.CREATED);
    }
}
