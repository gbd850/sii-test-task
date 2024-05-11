package com.test.sii.repository;

import com.test.sii.model.Currency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @AfterEach
    void tearDown() {
        currencyRepository.deleteAll();
    }

    @Test
    void givenValidCurrency_whenFindByCurrency_thenReturnCurrencyObject() {
        //given
        String currencyString = "USD";
        Currency currency = new Currency(null, currencyString);

        Currency expected = currencyRepository.save(currency);

        // when
        Optional<Currency> actual = currencyRepository.findByCurrency(currencyString);

        // then
        assertThat(actual)
                .isNotEmpty()
                .get()
                .isEqualTo(expected);
    }

    @Test
    void givenInvalidCurrency_whenFindByCurrency_thenReturnEmptyOptional() {
        //given
        String currencyString = "USD";

        // when
        Optional<Currency> actual = currencyRepository.findByCurrency(currencyString);

        // then
        assertThat(actual).isEmpty();
    }
}