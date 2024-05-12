package com.test.sii.service;

import com.test.sii.dto.PromoCodeDetailsResponse;
import com.test.sii.dto.PromoCodeRequest;
import com.test.sii.dto.PromoCodeResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.PromoCode;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.model.PromoCodePercentage;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final CurrencyRepository currencyRepository;

    public List<PromoCodeResponse> getAllPromoCodes() {
        return promoCodeRepository.findAll().stream()
                .map(promoCode -> new PromoCodeResponse(
                        promoCode.getCode(),
                        promoCode.getExpirationDate(),
                        promoCode.getAmount(),
                        promoCode.getCurrency().getCurrency(),
                        promoCode.getDiscountMethod()
                ))
                .toList();
    }

    @Transactional
    public PromoCodeResponse createMonetaryPromoCode(PromoCodeRequest promoCodeRequest) {
        Currency currency = currencyRepository.findByCurrency(promoCodeRequest.currency()).orElse(null);
        if (currency == null) {
            currency = currencyRepository.save(new Currency(null, promoCodeRequest.currency()));
        }

        PromoCode promoCode = new PromoCodeMonetary(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );

        try {
            promoCodeRepository.save(promoCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        return new PromoCodeResponse(
                promoCode.getCode(),
                promoCode.getExpirationDate(),
                promoCode.getAmount(),
                promoCode.getCurrency().getCurrency(),
                promoCode.getDiscountMethod()
        );
    }

    @Transactional
    public PromoCodeResponse createPercentagePromoCode(PromoCodeRequest promoCodeRequest) {
        Currency currency = currencyRepository.findByCurrency(promoCodeRequest.currency()).orElse(null);
        if (currency == null) {
            currency = currencyRepository.save(new Currency(null, promoCodeRequest.currency()));
        }

        PromoCode promoCode = new PromoCodePercentage(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );

        try {
            promoCodeRepository.save(promoCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        return new PromoCodeResponse(
                promoCode.getCode(),
                promoCode.getExpirationDate(),
                promoCode.getAmount(),
                promoCode.getCurrency().getCurrency(),
                promoCode.getDiscountMethod()
        );
    }

    public PromoCodeDetailsResponse getPromoCodeDetails(String code) {
        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found", new Exception("Promo code " + code + " was not found")));

        return new PromoCodeDetailsResponse(
                promoCode.getCode(),
                promoCode.getExpirationDate(),
                promoCode.getMaxUsages(),
                promoCode.getUsages(),
                promoCode.getAmount(),
                promoCode.getCurrency().getCurrency(),
                promoCode.getDiscountMethod()
        );
    }

}
