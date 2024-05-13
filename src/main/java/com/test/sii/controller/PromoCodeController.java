package com.test.sii.controller;

import com.test.sii.dto.PromoCodeDetailsResponse;
import com.test.sii.dto.PromoCodeRequest;
import com.test.sii.dto.PromoCodeResponse;
import com.test.sii.service.PromoCodeService;
import com.test.sii.util.PromoCodePattern;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/promo-codes")
@RequiredArgsConstructor
@Validated
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @GetMapping
    public ResponseEntity<List<PromoCodeResponse>> getAllPromoCodes(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        return new ResponseEntity<>(promoCodeService.getAllPromoCodes(page, size), HttpStatus.OK);
    }

    @PostMapping("monetary")
    public ResponseEntity<PromoCodeResponse> createMonetaryPromoCode(@Valid @RequestBody PromoCodeRequest promoCodeRequest) {
        return new ResponseEntity<>(promoCodeService.createMonetaryPromoCode(promoCodeRequest), HttpStatus.CREATED);
    }

    @PostMapping("percentage")
    public ResponseEntity<PromoCodeResponse> createPercentagePromoCode(@Valid @RequestBody PromoCodeRequest promoCodeRequest) {
        return new ResponseEntity<>(promoCodeService.createPercentagePromoCode(promoCodeRequest), HttpStatus.CREATED);
    }

    @GetMapping("/details/{promoCode}")
    public ResponseEntity<PromoCodeDetailsResponse> getPromoCodeDetails(@PathVariable("promoCode") @PromoCodePattern String promoCode) {
        return new ResponseEntity<>(promoCodeService.getPromoCodeDetails(promoCode), HttpStatus.OK);
    }
}
