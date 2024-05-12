package com.test.sii.service;

import com.test.sii.dto.ProductResponse;
import com.test.sii.dto.PurchaseDiscountResponse;
import com.test.sii.dto.PurchaseRequest;
import com.test.sii.dto.PurchaseResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.model.Purchase;
import com.test.sii.repository.ProductRepository;
import com.test.sii.repository.PromoCodeRepository;
import com.test.sii.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    private PurchaseService purchaseService;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseService(purchaseRepository, productRepository, promoCodeRepository);
    }

    @Test
    void givenValidProductIdAndValidPromoCode_whenGetDiscountPrice_thenReturnDiscountPriceResponse() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        Currency currency = new Currency(1, "USD");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency
        );

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(25.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PurchaseDiscountResponse actualResponse = purchaseService.getDiscountPrice(productId, code);

        // then
        PurchaseDiscountResponse expectedResponse = new PurchaseDiscountResponse(
                product.getPrice().subtract(promoCode.getAmount()).max(BigDecimal.ZERO),
                currency.getCurrency(),
                null
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenInvalidProductId_whenGetDiscountPrice_thenThrowException() {
        // given
        Integer productId = 1;

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> purchaseService.getDiscountPrice(productId, ""))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void givenValidProductIdAndInvalidPromoCode_whenGetDiscountPrice_thenThrowException() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        Currency currency = new Currency(1, "USD");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(promoCodeRepository.findByCode(any())).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> purchaseService.getDiscountPrice(productId, code))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void givenValidProductIdAndExpiredPromoCode_whenGetDiscountPrice_thenReturnDiscountPriceResponseWithNoDiscount() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        Currency currency = new Currency(1, "USD");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency
        );

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().minusYears(1)),
                100,
                BigDecimal.valueOf(25.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PurchaseDiscountResponse actualResponse = purchaseService.getDiscountPrice(productId, code);

        // then
        PurchaseDiscountResponse expectedResponse = new PurchaseDiscountResponse(
                product.getPrice(),
                currency.getCurrency(),
                null
        );

        assertThat(actualResponse.warning()).contains("expired");

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("warning")
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenValidProductIdAndPromoCodeWithMismatchCurrencies_whenGetDiscountPrice_thenReturnDiscountPriceResponseWithNoDiscount() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        Currency currency1 = new Currency(1, "USD");
        Currency currency2 = new Currency(2, "EUR");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency1
        );

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(25.00),
                currency2
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PurchaseDiscountResponse actualResponse = purchaseService.getDiscountPrice(productId, code);

        // then
        PurchaseDiscountResponse expectedResponse = new PurchaseDiscountResponse(
                product.getPrice(),
                currency1.getCurrency(),
                null
        );

        assertThat(actualResponse.warning())
                .containsIgnoringCase("currencies")
                .containsIgnoringCase("don't match");

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("warning")
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenValidPurchaseRequest_whenCreatePurchase_thenCreateNewPurchase() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                productId,
                code
        );

        Currency currency = new Currency(1, "USD");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(25.00),
                currency
        );

        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PurchaseResponse actualResponse = purchaseService.createPurchase(purchaseRequest);

        // then
        ArgumentCaptor<Purchase> purchaseArgumentCaptor = ArgumentCaptor.forClass(Purchase.class);

        verify(purchaseRepository).save(purchaseArgumentCaptor.capture());

        Purchase actual = purchaseArgumentCaptor.getValue();

        BigDecimal discountAmount = product.getPrice().compareTo(promoCode.getAmount()) < 0 ? product.getPrice() : promoCode.getAmount();

        Purchase expected = new Purchase(
                null,
                Date.valueOf(LocalDate.now()),
                product.getPrice(),
                discountAmount,
                product
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(expected);

        assertThat(actual.getDate()).isToday();

        PurchaseResponse expectedResponse = new PurchaseResponse(
                product.getPrice(),
                discountAmount,
                Date.valueOf(LocalDate.now()),
                new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency().getCurrency()
                ),
                null
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(expectedResponse);

        assertThat(actualResponse.date()).isToday();
    }

    @Test
    void givenValidPurchaseRequestWithExpiredPromoCode_whenCreatePurchase_thenCreateNewPurchaseWithNoDiscount() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                productId,
                code
        );

        Currency currency = new Currency(1, "USD");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().minusYears(1)),
                100,
                BigDecimal.valueOf(25.00),
                currency
        );

        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PurchaseResponse actualResponse = purchaseService.createPurchase(purchaseRequest);

        // then
        ArgumentCaptor<Purchase> purchaseArgumentCaptor = ArgumentCaptor.forClass(Purchase.class);

        verify(purchaseRepository).save(purchaseArgumentCaptor.capture());

        Purchase actual = purchaseArgumentCaptor.getValue();

        Purchase expected = new Purchase(
                null,
                Date.valueOf(LocalDate.now()),
                product.getPrice(),
                BigDecimal.ZERO,
                product
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(expected);

        assertThat(actual.getDate()).isToday();

        PurchaseResponse expectedResponse = new PurchaseResponse(
                product.getPrice(),
                BigDecimal.ZERO,
                Date.valueOf(LocalDate.now()),
                new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency().getCurrency()
                ),
                null
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("date", "warning")
                .isEqualTo(expectedResponse);

        assertThat(actualResponse.date()).isToday();

        assertThat(actualResponse.warning()).containsIgnoringCase("expired");
    }

    @Test
    void givenValidPurchaseRequestWithMismatchCurrencyPromoCode_whenCreatePurchase_thenCreateNewPurchaseWithNoDiscount() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                productId,
                code
        );

        Currency currency1 = new Currency(1, "USD");
        Currency currency2 = new Currency(2, "EUR");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency1
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(25.00),
                currency2
        );

        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PurchaseResponse actualResponse = purchaseService.createPurchase(purchaseRequest);

        // then
        ArgumentCaptor<Purchase> purchaseArgumentCaptor = ArgumentCaptor.forClass(Purchase.class);

        verify(purchaseRepository).save(purchaseArgumentCaptor.capture());

        Purchase actual = purchaseArgumentCaptor.getValue();

        Purchase expected = new Purchase(
                null,
                Date.valueOf(LocalDate.now()),
                product.getPrice(),
                BigDecimal.ZERO,
                product
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(expected);

        assertThat(actual.getDate()).isToday();

        PurchaseResponse expectedResponse = new PurchaseResponse(
                product.getPrice(),
                BigDecimal.ZERO,
                Date.valueOf(LocalDate.now()),
                new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency().getCurrency()
                ),
                null
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("date", "warning")
                .isEqualTo(expectedResponse);

        assertThat(actualResponse.date()).isToday();

        assertThat(actualResponse.warning())
                .containsIgnoringCase("currencies")
                .containsIgnoringCase("don't match");
    }

    @Test
    void givenInvalidPurchaseRequestWithInvalidProductId_whenCreatePurchase_thenThrowException() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                productId,
                code
        );

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> purchaseService.createPurchase(purchaseRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("Product");
    }

    @Test
    void givenValidPurchaseRequestWithOverusedPromoCode_whenCreatePurchase_thenCreateNewPurchaseWithNoDiscount() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                productId,
                code
        );

        Currency currency = new Currency(1, "USD");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().plusYears(1)),
                1,
                BigDecimal.valueOf(25.00),
                currency
        );

        promoCode.setUsages(1);

        given(promoCodeRepository.findByCode(code)).willReturn(Optional.of(promoCode));

        // when
        PurchaseResponse actualResponse = purchaseService.createPurchase(purchaseRequest);

        // then
        ArgumentCaptor<Purchase> purchaseArgumentCaptor = ArgumentCaptor.forClass(Purchase.class);

        verify(purchaseRepository).save(purchaseArgumentCaptor.capture());

        Purchase actual = purchaseArgumentCaptor.getValue();

        Purchase expected = new Purchase(
                null,
                Date.valueOf(LocalDate.now()),
                product.getPrice(),
                BigDecimal.ZERO,
                product
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(expected);

        assertThat(actual.getDate()).isToday();

        PurchaseResponse expectedResponse = new PurchaseResponse(
                product.getPrice(),
                BigDecimal.ZERO,
                Date.valueOf(LocalDate.now()),
                new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency().getCurrency()
                ),
                null
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("date", "warning")
                .isEqualTo(expectedResponse);

        assertThat(actualResponse.date()).isToday();

        assertThat(actualResponse.warning())
                .containsIgnoringCase("maximum usages");
    }

    @Test
    void givenInvalidPurchaseRequestWithInvalidPromoCode_whenCreatePurchase_thenThrowException() {
        // given
        Integer productId = 1;
        String code = "promoCodeExample";

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                productId,
                code
        );

        Currency currency = new Currency(1, "USD");

        Product product = new Product(
                1,
                "Product 1",
                "Description of Product 1",
                BigDecimal.valueOf(140.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        given(promoCodeRepository.findByCode(code)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> purchaseService.createPurchase(purchaseRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("Promo code");
    }
}