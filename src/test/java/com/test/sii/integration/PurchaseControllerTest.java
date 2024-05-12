package com.test.sii.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.sii.dto.ProductResponse;
import com.test.sii.dto.PurchaseRequest;
import com.test.sii.dto.PurchaseResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.model.PromoCodeMonetary;
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
import java.text.DateFormat;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
                code,
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
                .body("discountPrice", equalTo(discountPrice.floatValue()))
                .body("warning", nullValue());
    }

    @Test
    void givenInvalidProductIdAndValidPromoCode_whenGetDiscountPrice_thenReturnError() {
        // given
        String code = "promoCodeExample";
        int productId = 1;

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/purchases/discount?productId=" + productId + "&promoCode=" + code)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("title", containsStringIgnoringCase("product not found"));
    }

    @Test
    void givenValidProductIdAndInvalidPromoCode_whenGetDiscountPrice_thenReturnError() {
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

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/purchases/discount?productId=" + product.getId() + "&promoCode=" + code)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("title", containsStringIgnoringCase("promo code not found"));
    }

    @Test
    void givenValidProductIdAndValidExpiredPromoCode_whenGetDiscountPrice_thenReturnDiscountPriceWithNoDiscount() {
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
                code,
                Date.valueOf(LocalDate.now().minusYears(1)),
                100,
                BigDecimal.valueOf(20.0),
                currency
        );

        promoCodeRepository.save(promoCode);

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/purchases/discount?productId=" + product.getId() + "&promoCode=" + code)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("discountPrice", equalTo(product.getPrice().floatValue()))
                .body("warning", containsStringIgnoringCase("expired"));
    }

    @Test
    void givenValidProductIdAndValidPromoCodeWithCurrenciesMismatch_whenGetDiscountPrice_thenReturnDiscountPriceWithNoDiscount() {
        // given
        String code = "promoCodeExample";

        Currency currency1 = new Currency(null, "USD");
        currency1 = currencyRepository.save(currency1);

        Currency currency2 = new Currency(null, "EUR");
        currency2 = currencyRepository.save(currency2);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.0),
                currency1
        );

        product = productRepository.save(product);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(20.0),
                currency2
        );

        promoCodeRepository.save(promoCode);

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/purchases/discount?productId=" + product.getId() + "&promoCode=" + code)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("discountPrice", equalTo(product.getPrice().floatValue()))
                .body("warning", allOf(
                        containsStringIgnoringCase("currencies"),
                        containsStringIgnoringCase("don't match")
                ));
    }

    @Test
    void givenValidPurchaseRequest_whenCreatePurchase_thenCreateNewPurchase() {
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

        product = productRepository.save(product);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(20.0),
                currency
        );

        promoCode = promoCodeRepository.save(promoCode);

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                product.getId(),
                promoCode.getCode()
        );

        // when
        BigDecimal discountAmount = product.getPrice().compareTo(promoCode.getAmount()) < 0 ? product.getPrice() : promoCode.getAmount();

        PurchaseResponse expected = new PurchaseResponse(
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

        String expectedJSON;
        try {
            expectedJSON = objectMapper
                    .setDateFormat(DateFormat.getDateInstance())
                    .writeValueAsString(expected);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        given()
                .contentType(ContentType.JSON)
                .with()
                .body(purchaseRequest)
                .when()
                .post("/v1/api/purchases")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getMap("")));
    }

    @Test
    void givenInvalidPurchaseRequestWithInvalidProductId_whenCreatePurchase_thenReturnError() {
        // given
        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(20.0),
                currency
        );

        promoCode = promoCodeRepository.save(promoCode);

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                1,
                promoCode.getCode()
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(purchaseRequest)
                .when()
                .post("/v1/api/purchases")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("title", containsStringIgnoringCase("product not found"));
    }

    @Test
    void givenInvalidPurchaseRequestWithInvalidPromoCode_whenCreatePurchase_thenReturnError() {
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

        product = productRepository.save(product);

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                product.getId(),
                "invalidCode"
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(purchaseRequest)
                .when()
                .post("/v1/api/purchases")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("title", containsStringIgnoringCase("promo code not found"));
    }

    @Test
    void givenValidPurchaseRequestWithExpiredPromoCode_whenCreatePurchase_thenCreateNewPurchaseWithNoDiscountAmount() {
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

        product = productRepository.save(product);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().minusYears(1)),
                100,
                BigDecimal.valueOf(20.0),
                currency
        );

        promoCode = promoCodeRepository.save(promoCode);

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                product.getId(),
                promoCode.getCode()
        );

        // when
        ProductResponse expectedProduct = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );

        PurchaseResponse expected = new PurchaseResponse(
                product.getPrice(),
                BigDecimal.ZERO,
                Date.valueOf(LocalDate.now()),
                expectedProduct,
                null
        );

        String expectedProductJSON;
        try {
            expectedProductJSON = objectMapper
                    .setDateFormat(DateFormat.getDateInstance())
                    .writeValueAsString(expectedProduct);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        given()
                .contentType(ContentType.JSON)
                .with()
                .body(purchaseRequest)
                .when()
                .post("/v1/api/purchases")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("regularPrice", equalTo(expected.regularPrice().floatValue()))
                .body("discountAmount", equalTo(expected.discountAmount().intValue()))
                .body("date", equalTo(expected.date().toString()))
                .body("product", equalTo(JsonPath.from(expectedProductJSON).getMap("")))
                .body("warning", containsStringIgnoringCase("expired"));
    }

    @Test
    void givenValidPurchaseRequestWithMismatchCurrenciesPromoCode_whenCreatePurchase_thenCreateNewPurchaseWithNoDiscountAmount() {
        // given
        Currency currency1 = new Currency(null, "USD");
        currency1 = currencyRepository.save(currency1);

        Currency currency2 = new Currency(null, "EUR");
        currency2 = currencyRepository.save(currency2);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.0),
                currency1
        );

        product = productRepository.save(product);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(20.0),
                currency2
        );

        promoCode = promoCodeRepository.save(promoCode);

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                product.getId(),
                promoCode.getCode()
        );

        // when
        ProductResponse expectedProduct = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );

        PurchaseResponse expected = new PurchaseResponse(
                product.getPrice(),
                BigDecimal.ZERO,
                Date.valueOf(LocalDate.now()),
                expectedProduct,
                null
        );

        String expectedProductJSON;
        try {
            expectedProductJSON = objectMapper
                    .setDateFormat(DateFormat.getDateInstance())
                    .writeValueAsString(expectedProduct);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        given()
                .contentType(ContentType.JSON)
                .with()
                .body(purchaseRequest)
                .when()
                .post("/v1/api/purchases")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("regularPrice", equalTo(expected.regularPrice().floatValue()))
                .body("discountAmount", equalTo(expected.discountAmount().intValue()))
                .body("date", equalTo(expected.date().toString()))
                .body("product", equalTo(JsonPath.from(expectedProductJSON).getMap("")))
                .body("warning", allOf(
                        containsStringIgnoringCase("currencies"),
                        containsStringIgnoringCase("don't match")
                ));
    }

    @Test
    void givenValidPurchaseRequestWithOverusedPromoCode_whenCreatePurchase_thenCreateNewPurchaseWithNoDiscountAmount() {
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

        product = productRepository.save(product);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                1,
                BigDecimal.valueOf(20.0),
                currency
        );
        promoCode.setUsages(1);

        promoCode = promoCodeRepository.save(promoCode);

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                product.getId(),
                promoCode.getCode()
        );

        // when
        ProductResponse expectedProduct = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );

        PurchaseResponse expected = new PurchaseResponse(
                product.getPrice(),
                BigDecimal.ZERO,
                Date.valueOf(LocalDate.now()),
                expectedProduct,
                null
        );

        String expectedProductJSON;
        try {
            expectedProductJSON = objectMapper
                    .setDateFormat(DateFormat.getDateInstance())
                    .writeValueAsString(expectedProduct);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        given()
                .contentType(ContentType.JSON)
                .with()
                .body(purchaseRequest)
                .when()
                .post("/v1/api/purchases")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("regularPrice", equalTo(expected.regularPrice().floatValue()))
                .body("discountAmount", equalTo(expected.discountAmount().intValue()))
                .body("date", equalTo(expected.date().toString()))
                .body("product", equalTo(JsonPath.from(expectedProductJSON).getMap("")))
                .body("warning", containsStringIgnoringCase("maximum usages"));
    }
}