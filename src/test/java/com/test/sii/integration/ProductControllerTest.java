package com.test.sii.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.sii.dto.ProductCreateRequest;
import com.test.sii.dto.ProductRequest;
import com.test.sii.dto.ProductResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.ProductRepository;
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
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    Integer port;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        productRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @Test
    void givenExistingProducts_whenGetAllProducts_thenReturnProductsList() {
        // given
        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                currency
        );

        product = productRepository.save(product);

        // when
        ProductResponse expected = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );

        String expectedJSON;
        try {
            expectedJSON = objectMapper.writeValueAsString(List.of(expected));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/products")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getList("")));
    }

    @Test
    void givenNoExistingProducts_whenGetAllProducts_thenReturnEmptyList() {
        // given
        productRepository.deleteAll();

        // when
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/api/products")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", empty());
    }

    @Test
    void givenValidProductRequest_whenCreateProduct_thenCreateProduct() {
        // given
        ProductCreateRequest productRequest = new ProductCreateRequest(
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                "USD"
        );

        // when
        ProductResponse expected = new ProductResponse(
                null,
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                productRequest.currency()
        );

        given()
                .contentType(ContentType.JSON)
                .with()
                .body(productRequest)
                .when()
                .post("/v1/api/products")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("name", equalTo(expected.name()))
                .body("description", equalTo(expected.description()))
                .body("price", equalTo(expected.price().floatValue()))
                .body("currency", equalTo(expected.currency()));
    }

    @Test
    void givenValidProductRequestWithDuplicateName_whenCreateProduct_thenReturnError() {
        // given
        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                currency
        );

        productRepository.save(product);

        ProductCreateRequest productRequest = new ProductCreateRequest(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(productRequest)
                .when()
                .post("/v1/api/products")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void givenInvalidProductRequest_whenCreateProduct_thenReturnError() {
        // given
        ProductCreateRequest productRequest = new ProductCreateRequest(
                "",
                null,
                null,
                "USD"
        );

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(productRequest)
                .when()
                .post("/v1/api/products")

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void givenValidProductRequestAndValidId_whenUpdateProduct_thenUpdateProduct() {
        // given
        ProductRequest productRequest = new ProductRequest(
                "Product 2",
                null,
                null,
                null
        );

        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                currency
        );

        product = productRepository.save(product);

        // when
        ProductResponse expected = new ProductResponse(
                product.getId(),
                productRequest.name(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );

        String expectedJSON;
        try {
            expectedJSON = objectMapper.writeValueAsString(expected);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        given()
                .contentType(ContentType.JSON)
                .with()
                .body(productRequest)
                .when()
                .patch("/v1/api/products/" + product.getId())

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("", equalTo(JsonPath.from(expectedJSON).getMap("")));
    }

    @Test
    void givenValidProductRequestAndInvalidId_whenUpdateProduct_thenReturnError() {
        // given
        ProductRequest productRequest = new ProductRequest(
                "Product 2",
                null,
                null,
                null
        );

        Currency currency = new Currency(null, "USD");
        currency = currencyRepository.save(currency);

        Product product = new Product(
                null,
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                currency
        );

        product = productRepository.save(product);

        int productId = product.getId() + 1;

        // when
        given()
                .contentType(ContentType.JSON)
                .with()
                .body(productRequest)
                .when()
                .patch("/v1/api/products/" + productId)

                // then
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("title", containsStringIgnoringCase("product not found"));
    }
}