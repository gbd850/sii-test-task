package com.test.sii.service;

import com.test.sii.dto.SalesReportEntryResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.model.Purchase;
import com.test.sii.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SalesServiceTest {

    private SalesService salesService;

    @Mock
    private PurchaseRepository purchaseRepository;

    @BeforeEach
    void setUp() {
        salesService = new SalesService(purchaseRepository);
    }

    @Test
    void givenExistingPurchases_whenGetSalesReport_thenGenerateReport() {
        // given
        Purchase purchase1 = new Purchase(
                1,
                Date.valueOf(LocalDate.now().plusYears(1)),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(25.00),
                new Product(
                        1,
                        "Product 1",
                        null,
                        BigDecimal.valueOf(100.00),
                        new Currency(1, "USD")
                )
        );

        Purchase purchase2 = new Purchase(
                2,
                Date.valueOf(LocalDate.now().plusYears(1)),
                BigDecimal.valueOf(140.00),
                BigDecimal.valueOf(15.00),
                new Product(
                        2,
                        "Product 2",
                        null,
                        BigDecimal.valueOf(140.00),
                        new Currency(2, "EUR")
                )
        );

        List<SalesReportEntryResponse> expected = List.of(
                new SalesReportEntryResponse(
                        purchase1.getProduct().getCurrency().getCurrency(),
                        purchase1.getRegularPrice(),
                        purchase1.getDiscountAmount(),
                        1L
                ),
                new SalesReportEntryResponse(
                        purchase2.getProduct().getCurrency().getCurrency(),
                        purchase2.getRegularPrice(),
                        purchase2.getDiscountAmount(),
                        1L
                )
        );

        given(purchaseRepository.generateSalesReport()).willReturn(expected);

        // when
        List<SalesReportEntryResponse> actual = salesService.getSalesReport();

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

    }

    @Test
    void givenNoExistingPurchases_whenGetSalesReport_thenGenerateEmptyReport() {
        // given
        given(purchaseRepository.generateSalesReport()).willReturn(List.of());

        // when
        List<SalesReportEntryResponse> actual = salesService.getSalesReport();

        // then
        assertThat(actual).isEmpty();

    }
}