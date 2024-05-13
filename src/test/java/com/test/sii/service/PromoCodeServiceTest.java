package com.test.sii.service;

import com.test.sii.dto.DiscountMethod;
import com.test.sii.dto.PromoCodeDetailsResponse;
import com.test.sii.dto.PromoCodeRequest;
import com.test.sii.dto.PromoCodeResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.PromoCode;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.model.PromoCodePercentage;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    private PromoCodeService promoCodeService;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        promoCodeService = new PromoCodeService(promoCodeRepository, currencyRepository);
    }

    @Test
    void givenExistingPromoCodes_whenGetAllPromoCodes_thenReturnPromoCodesList() {
        // given
        PromoCodeMonetary promoCodeMonetary = new PromoCodeMonetary(
                "promoCodeMExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(45.00),
                new Currency(1, "USD")
        );

        PromoCodePercentage promoCodePercentage = new PromoCodePercentage(
                "promoCodePExample",
                Date.valueOf("2024-10-10"),
                200,
                BigDecimal.valueOf(25.00),
                new Currency(1, "USD")
        );

        given(promoCodeRepository.findAll()).willReturn(List.of(promoCodeMonetary, promoCodePercentage));

        // when
        List<PromoCodeResponse> actual = promoCodeService.getAllPromoCodes(null, null);

        // then
        List<PromoCodeResponse> expected = List.of(
                new PromoCodeResponse(
                        promoCodeMonetary.getCode(),
                        promoCodeMonetary.getExpirationDate(),
                        promoCodeMonetary.getAmount(),
                        promoCodeMonetary.getCurrency().getCurrency(),
                        promoCodeMonetary.getDiscountMethod()
                ),
                new PromoCodeResponse(
                        promoCodePercentage.getCode(),
                        promoCodePercentage.getExpirationDate(),
                        promoCodePercentage.getAmount(),
                        promoCodePercentage.getCurrency().getCurrency(),
                        promoCodePercentage.getDiscountMethod()
                )
        );

        assertThat(actual)
                .hasSize(expected.size())
                .hasSameElementsAs(expected);
    }

    @Test
    void givenExistingPromoCodesWithPageAndSize_whenGetAllPromoCodes_thenReturnPaginatedPromoCodesList() {
        // given
        PromoCodeMonetary promoCodeMonetary = new PromoCodeMonetary(
                "promoCodeMExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(45.00),
                new Currency(1, "USD")
        );

        PromoCodePercentage promoCodePercentage = new PromoCodePercentage(
                "promoCodePExample",
                Date.valueOf("2024-10-10"),
                200,
                BigDecimal.valueOf(25.00),
                new Currency(1, "USD")
        );

        int page = 1;
        int size = 1;

        given(promoCodeRepository.findAll(PageRequest.of(page - 1, size))).willReturn(new PageImpl<>(List.of(promoCodeMonetary)));

        // when
        List<PromoCodeResponse> actual = promoCodeService.getAllPromoCodes(page, size);

        // then
        List<PromoCodeResponse> expected = List.of(
                new PromoCodeResponse(
                        promoCodeMonetary.getCode(),
                        promoCodeMonetary.getExpirationDate(),
                        promoCodeMonetary.getAmount(),
                        promoCodeMonetary.getCurrency().getCurrency(),
                        promoCodeMonetary.getDiscountMethod()
                )
        );

        assertThat(actual)
                .hasSize(expected.size())
                .hasSameElementsAs(expected);
    }

    @Test
    void givenNoPromoCodes_whenGetAllPromoCodes_thenReturnEmptyList() {
        // given
        given(promoCodeRepository.findAll()).willReturn(List.of());

        // when
        List<PromoCodeResponse> actual = promoCodeService.getAllPromoCodes(null, null);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void givenPageNumberWithNoPageSize_whenGetAllPromoCodes_thenThrowException() {
        // given
        Integer page = 1;
        Integer size = null;

        // when
        // then
        assertThatThrownBy(() -> promoCodeService.getAllPromoCodes(page, size))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void givenNegativePageNumberWithPageSize_whenGetAllPromoCodes_thenThrowException() {
        // given
        Integer page = -1;
        Integer size = 1;

        // when
        // then
        assertThatThrownBy(() -> promoCodeService.getAllPromoCodes(page, size))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void givenValidPromoCodeRequestWithExistingCurrency_whenCreateMonetaryPromoCode_thenCreateNewPromoCode() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(25.00),
                "USD"
        );

        Currency currency = new Currency(1, promoCodeRequest.currency());

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.of(currency));

        // when
        PromoCodeResponse actualResponse = promoCodeService.createMonetaryPromoCode(promoCodeRequest);

        // then
        ArgumentCaptor<PromoCodeMonetary> promoCodeMonetaryArgumentCaptor = ArgumentCaptor.forClass(PromoCodeMonetary.class);

        verify(promoCodeRepository).save(promoCodeMonetaryArgumentCaptor.capture());

        PromoCodeMonetary actual = promoCodeMonetaryArgumentCaptor.getValue();

        PromoCodeMonetary expected = new PromoCodeMonetary(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        PromoCodeResponse expectedResponse = new PromoCodeResponse(
                expected.getCode(),
                expected.getExpirationDate(),
                expected.getAmount(),
                expected.getCurrency().getCurrency(),
                DiscountMethod.MONETARY
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenValidPromoCodeRequestWithNewCurrency_whenCreateMonetaryPromoCode_thenCreateNewPromoCode() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(25.00),
                "USD"
        );

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.empty());

        Currency currency = new Currency(1, promoCodeRequest.currency());

        given(currencyRepository.save(any())).willReturn(currency);

        // when
        PromoCodeResponse actualResponse = promoCodeService.createMonetaryPromoCode(promoCodeRequest);

        // then
        ArgumentCaptor<PromoCodeMonetary> promoCodeMonetaryArgumentCaptor = ArgumentCaptor.forClass(PromoCodeMonetary.class);

        verify(promoCodeRepository).save(promoCodeMonetaryArgumentCaptor.capture());

        PromoCodeMonetary actual = promoCodeMonetaryArgumentCaptor.getValue();

        PromoCodeMonetary expected = new PromoCodeMonetary(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        PromoCodeResponse expectedResponse = new PromoCodeResponse(
                expected.getCode(),
                expected.getExpirationDate(),
                expected.getAmount(),
                expected.getCurrency().getCurrency(),
                DiscountMethod.MONETARY
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenPromoCodeRequestWithDuplicateCode_whenCreateMonetaryPromoCode_thenThrowException() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(25.00),
                "USD"
        );

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.empty());

        given(promoCodeRepository.save(any())).willThrow(new RuntimeException());

        // when
        // then
        assertThatThrownBy(() -> promoCodeService.createMonetaryPromoCode(promoCodeRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void givenValidPromoCodeRequestWithExistingCurrency_whenCreatePercentagePromoCode_thenCreateNewPromoCode() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(25.00),
                "USD"
        );

        Currency currency = new Currency(1, promoCodeRequest.currency());

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.of(currency));

        // when
        PromoCodeResponse actualResponse = promoCodeService.createPercentagePromoCode(promoCodeRequest);

        // then
        ArgumentCaptor<PromoCodePercentage> promoCodePercentageArgumentCaptor = ArgumentCaptor.forClass(PromoCodePercentage.class);

        verify(promoCodeRepository).save(promoCodePercentageArgumentCaptor.capture());

        PromoCodePercentage actual = promoCodePercentageArgumentCaptor.getValue();

        PromoCodePercentage expected = new PromoCodePercentage(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        PromoCodeResponse expectedResponse = new PromoCodeResponse(
                expected.getCode(),
                expected.getExpirationDate(),
                expected.getAmount(),
                expected.getCurrency().getCurrency(),
                DiscountMethod.PERCENTAGE
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenValidPromoCodeRequestWithNewCurrency_whenCreatePercentagePromoCode_thenCreateNewPromoCode() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(25.00),
                "USD"
        );

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.empty());

        Currency currency = new Currency(1, promoCodeRequest.currency());

        given(currencyRepository.save(any())).willReturn(currency);

        // when
        PromoCodeResponse actualResponse = promoCodeService.createPercentagePromoCode(promoCodeRequest);

        // then
        ArgumentCaptor<PromoCodePercentage> promoCodePercentageArgumentCaptor = ArgumentCaptor.forClass(PromoCodePercentage.class);

        verify(promoCodeRepository).save(promoCodePercentageArgumentCaptor.capture());

        PromoCodePercentage actual = promoCodePercentageArgumentCaptor.getValue();

        PromoCodePercentage expected = new PromoCodePercentage(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        PromoCodeResponse expectedResponse = new PromoCodeResponse(
                expected.getCode(),
                expected.getExpirationDate(),
                expected.getAmount(),
                expected.getCurrency().getCurrency(),
                DiscountMethod.PERCENTAGE
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenPromoCodeRequestWithDuplicateCode_whenCreatePercentagePromoCode_thenThrowException() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(25.00),
                "USD"
        );

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.empty());

        given(promoCodeRepository.save(any())).willThrow(new RuntimeException());

        // when
        // then
        assertThatThrownBy(() -> promoCodeService.createPercentagePromoCode(promoCodeRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void givenValidPromoCode_whenGetPromoCodeDetails_thenReturnPromoCodeDetails() {
        // given
        String code = "promoCodeExample";

        PromoCode promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf("2024-10-10"),
                100,
                BigDecimal.valueOf(25.00),
                new Currency(1, "USD")
        );

        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PromoCodeDetailsResponse actualResponse = promoCodeService.getPromoCodeDetails(code);

        // then
        PromoCodeDetailsResponse expectedResponse = new PromoCodeDetailsResponse(
                promoCode.getCode(),
                promoCode.getExpirationDate(),
                promoCode.getMaxUsages(),
                promoCode.getUsages(),
                promoCode.getAmount(),
                promoCode.getCurrency().getCurrency(),
                promoCode.getDiscountMethod()
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenInvalidPromoCode_whenGetPromoCodeDetails_thenThrowException() {
        // given
        String code = "promoCodeExample";

        given(promoCodeRepository.findByCode(code)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> promoCodeService.getPromoCodeDetails(code))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }
}