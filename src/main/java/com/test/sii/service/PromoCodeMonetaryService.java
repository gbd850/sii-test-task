package com.test.sii.service;

import com.test.sii.dto.PromoCodeMonetaryDetailsResponse;
import com.test.sii.dto.PromoCodeMonetaryRequest;
import com.test.sii.dto.PromoCodeMonetaryResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.PromoCodeMonetaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeMonetaryService {

    private final PromoCodeMonetaryRepository promoCodeMonetaryRepository;
    private final CurrencyRepository currencyRepository;

    public List<PromoCodeMonetaryResponse> getAllMonetaryPromoCodes() {
        return promoCodeMonetaryRepository.findAll().stream()
                .map(promoCode -> new PromoCodeMonetaryResponse(
                        promoCode.getCode(),
                        promoCode.getExpirationDate(),
                        promoCode.getAmount(),
                        promoCode.getCurrency().getCurrency()
                ))
                .toList();
    }

    public PromoCodeMonetaryResponse createMonetaryPromoCode(PromoCodeMonetaryRequest promoCodeMonetaryRequest) {
        Currency currency = currencyRepository.findByCurrency(promoCodeMonetaryRequest.currency())
                .orElse(new Currency(null, promoCodeMonetaryRequest.currency()));

        PromoCodeMonetary promoCodeMonetary = new PromoCodeMonetary(
                promoCodeMonetaryRequest.code(),
                promoCodeMonetaryRequest.expirationDate(),
                promoCodeMonetaryRequest.maxUsages(),
                promoCodeMonetaryRequest.amount(),
                currency
        );

        try {
            promoCodeMonetaryRepository.save(promoCodeMonetary);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), new Exception(e.getCause().getMessage()));
        }

        return new PromoCodeMonetaryResponse(
                promoCodeMonetary.getCode(),
                promoCodeMonetary.getExpirationDate(),
                promoCodeMonetary.getAmount(),
                promoCodeMonetary.getCurrency().getCurrency()
        );
    }

    public PromoCodeMonetaryDetailsResponse getPromoCodeDetails(String promoCode) {
        PromoCodeMonetary promoCodeMonetary = promoCodeMonetaryRepository.findByCode(promoCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found", new Exception("Promo code " + promoCode + " was not found")));

        return new PromoCodeMonetaryDetailsResponse(
                promoCodeMonetary.getCode(),
                promoCodeMonetary.getExpirationDate(),
                promoCodeMonetary.getMaxUsages(),
                promoCodeMonetary.getUsages(),
                promoCodeMonetary.getAmount(),
                promoCodeMonetary.getCurrency().getCurrency()
        );
    }
}
