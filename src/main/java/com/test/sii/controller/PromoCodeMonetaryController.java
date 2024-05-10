package com.test.sii.controller;

import com.test.sii.dto.PromoCodeMonetaryDetailsResponse;
import com.test.sii.dto.PromoCodeMonetaryRequest;
import com.test.sii.dto.PromoCodeMonetaryResponse;
import com.test.sii.service.PromoCodeMonetaryService;
import com.test.sii.util.PromoCodePattern;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/promo-codes/monetary")
@RequiredArgsConstructor
@Validated
public class PromoCodeMonetaryController {

    private final PromoCodeMonetaryService promoCodeMonetaryService;

    @GetMapping
    public ResponseEntity<List<PromoCodeMonetaryResponse>> getAllMonetaryPromoCodes() {
        return new ResponseEntity<>(promoCodeMonetaryService.getAllMonetaryPromoCodes(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PromoCodeMonetaryResponse> createMonetaryPromoCode(@Valid @RequestBody PromoCodeMonetaryRequest promoCodeMonetaryRequest) {
        return new ResponseEntity<>(promoCodeMonetaryService.createMonetaryPromoCode(promoCodeMonetaryRequest), HttpStatus.CREATED);
    }

    @GetMapping("/details/{promoCode}")
    public ResponseEntity<PromoCodeMonetaryDetailsResponse> getPromoCodeDetails(@PathVariable("promoCode") @PromoCodePattern String promoCode) {
        return new ResponseEntity<>(promoCodeMonetaryService.getPromoCodeDetails(promoCode), HttpStatus.OK);
    }
}
