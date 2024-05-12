package com.test.sii.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.ProductRepository;
import com.test.sii.repository.PromoCodeRepository;
import com.test.sii.repository.PurchaseRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PurchaseControllerTest {

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
        productRepository.deleteAll();
        currencyRepository.deleteAll();
        purchaseRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        promoCodeRepository.deleteAll();
        productRepository.deleteAll();
        currencyRepository.deleteAll();
        purchaseRepository.deleteAll();
    }

    @Test
    void givenValidProductIdAndValidPromoCode_whenGetDiscountPrice_thenReturnDiscountPrice() {
        // given
        String code = "promoCodeExample";

        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.0),
                currency
        );

        product = productRepository.save(product);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(20.0),
                currency
        );

        promoCode = promoCodeRepository.save(promoCode);

        // when
        BigDecimal discountPrice = product.getPrice().subtract(promoCode.getAmount()).max(BigDecimal.ZERO);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/purchases/discount?productId=" + product.getId() + "&promoCode=" + code)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("discountPrice", equalTo(discountPrice.floatValue()));
    }

    @Test
    void createPurchase() {
    }
}