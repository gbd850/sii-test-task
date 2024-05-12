package com.test.sii.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.sii.dto.DiscountMethod;
import com.test.sii.dto.PromoCodeDetailsResponse;
import com.test.sii.dto.PromoCodeRequest;
import com.test.sii.dto.PromoCodeResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.PromoCodeMonetary;
import com.test.sii.model.PromoCodePercentage;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.PromoCodeRepository;
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
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PromoCodeControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    Integer port;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        promoCodeRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        promoCodeRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @Test
    void givenExistingPromoCodes_whenGetAllPromoCodes_thenReturnPromoCodesList() {
        // given
        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(45.00),
                currency
        );
        promoCodeRepository.save(promoCode);

        // when
        List<PromoCodeResponse> expected = List.of(
                new PromoCodeResponse(
                        promoCode.getCode(),
                        promoCode.getExpirationDate(),
                        promoCode.getAmount(),
                        promoCode.getCurrency().getCurrency(),
                        promoCode.getDiscountMethod()
                )
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
                .when()
                .get("/v1/api/promo-codes")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getList("")));
    }

    @Test
    void givenNoExistingPromoCodes_whenGetAllPromoCodes_thenReturnEmptyList() {
        // given
        promoCodeRepository.deleteAll();

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/promo-codes")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", empty());
    }

    @Test
    void givenValidPromoCodeRequest_whenCreateMonetaryPromoCode_thenCreateMonetaryPromoCode() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(10.00),
                "USD"
        );

        // when
        PromoCodeResponse expected = new PromoCodeResponse(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.amount(),
                promoCodeRequest.currency(),
                DiscountMethod.MONETARY
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
                .body(promoCodeRequest)
                .when()
                .post("/v1/api/promo-codes/monetary")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getMap("")));
    }

    @Test
    void givenValidPromoCodeRequestWithDuplicateName_whenCreateMonetaryPromoCode_thenReturnError() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(10.00),
                "USD"
        );

        Currency currency = new Currency(null, promoCodeRequest.currency());
        currency = currencyRepository.save(currency);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );
        promoCodeRepository.save(promoCode);

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(promoCodeRequest)
                .when()
                .post("/v1/api/promo-codes/monetary")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void givenInvalidPromoCodeRequest_whenCreateMonetaryPromoCode_thenReturnError() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "",
                null,
                100,
                null,
                ""
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(promoCodeRequest)
                .when()
                .post("/v1/api/promo-codes/monetary")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void givenValidPromoCodeRequest_whenCreatePercentagePromoCode_thenCreatePercentagePromoCode() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(10.00),
                "USD"
        );

        // when
        PromoCodeResponse expected = new PromoCodeResponse(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.amount(),
                promoCodeRequest.currency(),
                DiscountMethod.PERCENTAGE
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
                .body(promoCodeRequest)
                .when()
                .post("/v1/api/promo-codes/percentage")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getMap("")));
    }

    @Test
    void givenValidPromoCodeRequestWithDuplicateName_whenCreatePercentagePromoCode_thenReturnError() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "promoCodeExample",
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(10.00),
                "USD"
        );

        Currency currency = new Currency(null, promoCodeRequest.currency());
        currency = currencyRepository.save(currency);

        PromoCodePercentage promoCode = new PromoCodePercentage(
                promoCodeRequest.code(),
                promoCodeRequest.expirationDate(),
                promoCodeRequest.maxUsages(),
                promoCodeRequest.amount(),
                currency
        );
        promoCodeRepository.save(promoCode);

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(promoCodeRequest)
                .when()
                .post("/v1/api/promo-codes/percentage")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void givenInvalidPromoCodeRequest_whenCreatePercentagePromoCode_thenReturnError() {
        // given
        PromoCodeRequest promoCodeRequest = new PromoCodeRequest(
                "",
                null,
                100,
                null,
                ""
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(promoCodeRequest)
                .when()
                .post("/v1/api/promo-codes/percentage")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void givenValidPromoCode_whenGetPromoCodeDetails_thenReturnPromoCodeDetails() {
        // given
        String code = "promoCodeExample";

        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        PromoCodeMonetary promoCode = new PromoCodeMonetary(
                code,
                Date.valueOf(LocalDate.now().plusYears(1)),
                100,
                BigDecimal.valueOf(10.0),
                currency
        );
        promoCodeRepository.save(promoCode);

        // when
        PromoCodeDetailsResponse expected = new PromoCodeDetailsResponse(
                promoCode.getCode(),
                promoCode.getExpirationDate(),
                promoCode.getMaxUsages(),
                promoCode.getUsages(),
                promoCode.getAmount(),
                promoCode.getCurrency().getCurrency(),
                promoCode.getDiscountMethod()
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
                .when()
                .get("/v1/api/promo-codes/details/" + code)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getMap("")));
    }

    @Test
    void givenInvalidPromoCode_whenGetPromoCodeDetails_thenReturnError() {
        // given
        String code = "promoCodeExample";

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/promo-codes/details/" + code)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("title", containsStringIgnoringCase("promo code not found"));
    }
}