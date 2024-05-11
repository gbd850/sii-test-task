package com.test.sii.repository;

import com.test.sii.model.Currency;
import com.test.sii.model.PromoCode;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.model.PromoCodePercentage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PromoCodeRepositoryTest {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @AfterEach
    void tearDown() {
        promoCodeRepository.deleteAll();
    }

    @Test
    void givenValidMonetaryPromoCode_whenFindByCode_thenReturnPromoCodeMonetary() {
        // given
        String code = "promoCodeExample";
        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf("2024-10-15"),
                10,
                BigDecimal.valueOf(45.0),
                new Currency(null, "USD")
        );

        PromoCode expected = promoCodeRepository.save(promoCode);

        // when
        Optional<PromoCode> actual = promoCodeRepository.findByCode(code);

        // then
        assertThat(actual)
                .isNotEmpty()
                .get()
                .isInstanceOf(PromoCodeMonetary.class)
                .isEqualTo(expected);
    }

    @Test
    void givenValidPercentagePromoCode_whenFindByCode_thenReturnPromoCodePercentage() {
        // given
        String code = "promoCodeExample";
        PromoCodePercentage promoCode = new PromoCodePercentage(
                code,
                Date.valueOf("2024-10-15"),
                10,
                BigDecimal.valueOf(45.0),
                new Currency(null, "USD")
        );

        PromoCode expected = promoCodeRepository.save(promoCode);

        // when
        Optional<PromoCode> actual = promoCodeRepository.findByCode(code);

        // then
        assertThat(actual)
                .isNotEmpty()
                .get()
                .isInstanceOf(PromoCodePercentage.class)
                .isEqualTo(expected);
    }

    @Test
    void givenInvalidMonetaryPromoCode_whenFindByCode_thenReturnEmptyOptional() {
        // given
        String code = "promoCodeExample";

        // when
        Optional<PromoCode> actual = promoCodeRepository.findByCode(code);

        // then
        assertThat(actual).isEmpty();
    }
}