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

    private PromoCode getPromoCode(String code) {
        return promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found", new Exception("Promo code " + code + " was not found")));
    }

    private Currency getCurrency(String currency) {
        Currency curr = currencyRepository.findByCurrency(currency).orElse(null);
        if (curr == null) {
            curr = currencyRepository.save(new Currency(null, currency));
        }
        return curr;
    }

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
        Currency currency = getCurrency(promoCodeRequest.currency());

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
        Currency currency = getCurrency(promoCodeRequest.currency());

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
        PromoCode promoCode = getPromoCode(code);

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
