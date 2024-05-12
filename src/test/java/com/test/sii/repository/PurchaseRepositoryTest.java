package com.test.sii.repository;

import com.test.sii.dto.SalesReportEntryResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.model.Purchase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PurchaseRepositoryTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @AfterEach
    void tearDown() {
        purchaseRepository.deleteAll();
    }

    @Test
    void givenExistingPurchases_whenGenerateSalesReport_thenReturnSalesReportEntriesList() {
        // given
        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.0),
                currency
        );
        final Product finalProduct = productRepository.save(product);

        final Purchase purchase1 = new Purchase(
                null,
                Date.valueOf("2024-10-15"),
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(50.0),
                finalProduct
                );
        final Purchase purchase2 = new Purchase(
                null,
                Date.valueOf("2024-10-17"),
                BigDecimal.valueOf(140.00),
                BigDecimal.valueOf(35.00),
                finalProduct
        );

        purchaseRepository.saveAll(List.of(purchase1, purchase2));

        // when
        List<SalesReportEntryResponse> actual = purchaseRepository.generateSalesReport();

        // then
        List<SalesReportEntryResponse> expected = List.of(
                new SalesReportEntryResponse(
                        "USD",
                        purchase1.getRegularPrice().add(purchase2.getRegularPrice()).setScale(2, RoundingMode.HALF_UP),
                        purchase1.getDiscountAmount().add(purchase2.getDiscountAmount()).setScale(2, RoundingMode.HALF_UP),
                        2L
                )
        );

        assertThat(actual)
                .hasSize(1)
                .hasSameElementsAs(expected);
    }

    @Test
    void givenExistingMultiCurrencyPurchases_whenGenerateSalesReport_thenReturnSalesReportEntriesList() {
        // given
        Currency currency1 = new Currency(null, "USD");
        currency1 = currencyRepository.save(currency1);

        Currency currency2 = new Currency(null, "EUR");
        currency2 = currencyRepository.save(currency2);

        final Product product1 = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.0),
                currency1
        );
        final Product product2 = new Product(
                null,
                "Product 2",
                null,
                BigDecimal.valueOf(150.0),
                currency2
        );

        productRepository.saveAll(List.of(product1, product2));

        final Purchase purchase1 = new Purchase(
                null,
                Date.valueOf("2024-10-15"),
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(50.0),
                product1
        );
        final Purchase purchase2 = new Purchase(
                null,
                Date.valueOf("2024-10-17"),
                BigDecimal.valueOf(140.00),
                BigDecimal.valueOf(35.00),
                product2
        );

        purchaseRepository.saveAll(List.of(purchase1, purchase2));

        // when
        List<SalesReportEntryResponse> actual = purchaseRepository.generateSalesReport();

        // then
        List<SalesReportEntryResponse> expected = List.of(
                new SalesReportEntryResponse(
                        "USD",
                        purchase1.getRegularPrice().setScale(2, RoundingMode.HALF_UP),
                        purchase1.getDiscountAmount().setScale(2, RoundingMode.HALF_UP),
                        1L
                ),
                new SalesReportEntryResponse(
                        "EUR",
                        purchase2.getRegularPrice().setScale(2, RoundingMode.HALF_UP),
                        purchase2.getDiscountAmount().setScale(2, RoundingMode.HALF_UP),
                        1L
                )
        );

        assertThat(actual)
                .hasSize(2)
                .hasSameElementsAs(expected);
    }
}