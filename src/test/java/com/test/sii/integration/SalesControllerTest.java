package com.test.sii.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.sii.dto.SalesReportEntryResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.model.Purchase;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.ProductRepository;
import com.test.sii.repository.PromoCodeRepository;
import com.test.sii.repository.PurchaseRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SalesControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    Integer port;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        promoCodeRepository.deleteAll();
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        promoCodeRepository.deleteAll();
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @Test
    void givenExistingPurchases_whenGetSalesReport_thenReturnReport() {
        // given
        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        Product product1 = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                currency
        );

        product1 = productRepository.save(product1);

        Purchase purchase1 = new Purchase(
                null,
                Date.valueOf(LocalDate.now().plusYears(1)),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(25.00),
                product1
        );

        purchase1 = purchaseRepository.save(purchase1);

        Product product2 = new Product(
                null,
                "Product 2",
                null,
                BigDecimal.valueOf(100.00),
                currency
        );

        product2 = productRepository.save(product2);

        Purchase purchase2 = new Purchase(
                null,
                Date.valueOf(LocalDate.now().plusYears(1)),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(25.00),
                product2
        );

        purchase2 = purchaseRepository.save(purchase2);

        // when
        List<SalesReportEntryResponse> expected = List.of(
                new SalesReportEntryResponse(
                        currency.getCurrency(),
                        purchase1.getRegularPrice().add(purchase2.getRegularPrice()),
                        purchase1.getDiscountAmount().add(purchase2.getDiscountAmount()),
                        2L
                )
        );

        String expectedJSON;
        try {
            expectedJSON = objectMapper.writeValueAsString(expected);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/sales/report")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getList("")));
    }

    @Test
    void givenNoExistingPurchases_whenGetSalesReport_thenReturnEmptyReport() {
        // given
        purchaseRepository.deleteAll();

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/sales/report")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", empty());
    }
}