package com.test.sii.service;

import com.test.sii.dto.ProductCreateRequest;
import com.test.sii.dto.ProductRequest;
import com.test.sii.dto.ProductResponse;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, currencyRepository);
    }

    @Test
    void givenExistingProducts_whenGetAllProducts_thenReturnProductsList() {
        // given
        Product product = new Product(
                1,
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                new Currency(null, "USD")
        );


        given(productRepository.findAll()).willReturn(List.of(product));

        // when
        List<ProductResponse> actual = productService.getAllProducts();

        // then
        List<ProductResponse> expected = List.of(
                new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency().getCurrency()
                )
        );

        assertThat(actual).hasSameElementsAs(expected);
    }

    @Test
    void givenNoExistingProducts_whenGetAllProducts_thenReturnEmptyList() {
        // given
        given(productRepository.findAll()).willReturn(List.of());

        // when
        List<ProductResponse> actual = productService.getAllProducts();

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void givenValidProductRequestWithNewCurrency_whenCreateProduct_thenCreateNewProduct() {
        // given
        ProductCreateRequest productRequest = new ProductCreateRequest(
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                "USD"
        );

        Currency currency = new Currency(1, "USD");

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.empty());
        given(currencyRepository.save(any())).willReturn(currency);

        Product expected = new Product(
                1,
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                currency
        );

        given(productRepository.save(any())).willReturn(expected);

        // when
        ProductResponse actualResponse = productService.createProduct(productRequest);

        // then
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productArgumentCaptor.capture());

        Product actual = productArgumentCaptor.getValue();

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "currency.id")
                .isEqualTo(expected);

        ProductResponse expectedResponse = new ProductResponse(
                expected.getId(),
                expected.getName(),
                expected.getDescription(),
                expected.getPrice(),
                expected.getCurrency().getCurrency()
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void givenValidProductRequestWithExistingCurrency_whenCreateProduct_thenCreateNewProduct() {
        // given
        ProductCreateRequest productRequest = new ProductCreateRequest(
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                "USD"
        );

        Currency currency = new Currency(1, "USD");

        given(currencyRepository.findByCurrency(currency.getCurrency())).willReturn(Optional.of(currency));

        Product expected = new Product(
                1,
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                currency
        );

        given(productRepository.save(any())).willReturn(expected);

        // when
        ProductResponse actualResponse = productService.createProduct(productRequest);

        // then
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productArgumentCaptor.capture());

        Product actual = productArgumentCaptor.getValue();

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);

        ProductResponse expectedResponse = new ProductResponse(
                expected.getId(),
                expected.getName(),
                expected.getDescription(),
                expected.getPrice(),
                expected.getCurrency().getCurrency()
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void givenProductRequestWithDuplicateName_whenCreateProduct_thenThrowException() {
        // given
        ProductCreateRequest productRequest = new ProductCreateRequest(
                "Product 1",
                null,
                BigDecimal.valueOf(100.00),
                "USD"
        );

        given(productRepository.save(any())).willThrow(new RuntimeException());

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void givenValidProductIdAndValidProductRequestWithExistingCurrency_whenUpdateProduct_thenUpdateProduct() {
        // given
        Integer productId = 1;
        ProductRequest productRequest = new ProductRequest(
                "Product 1",
                null,
                null,
                null
        );

        Currency currency = new Currency(1, "USD");

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.empty());
        given(currencyRepository.save(any())).willReturn(currency);

        Product product = new Product(
                1,
                "Product 2",
                "Description of Product 2",
                BigDecimal.valueOf(100.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        Product expected = new Product(
                product.getId(),
                productRequest.name(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency()
        );

        given(productRepository.save(any())).willReturn(expected);

        // when
        ProductResponse actualResponse = productService.updateProduct(productId, productRequest);

        // then
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productArgumentCaptor.capture());

        Product actual = productArgumentCaptor.getValue();


        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        ProductResponse expectedResponse = new ProductResponse(
                expected.getId(),
                expected.getName(),
                expected.getDescription(),
                expected.getPrice(),
                expected.getCurrency().getCurrency()
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenValidProductIdAndValidProductRequestWithNewCurrency_whenUpdateProduct_thenUpdateProduct() {
        // given
        Integer productId = 1;
        ProductRequest productRequest = new ProductRequest(
                "Product 1",
                null,
                null,
                "EUR"
        );

        Currency currency = new Currency(1, "USD");

        given(currencyRepository.findByCurrency(any())).willReturn(Optional.empty());
        given(currencyRepository.save(any())).willReturn(currency);

        Product product = new Product(
                1,
                "Product 2",
                "Description of Product 2",
                BigDecimal.valueOf(100.00),
                currency
        );

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        Product expected = new Product(
                product.getId(),
                productRequest.name(),
                product.getDescription(),
                product.getPrice(),
                currency
        );

        given(productRepository.save(any())).willReturn(expected);

        // when
        ProductResponse actualResponse = productService.updateProduct(productId, productRequest);

        // then
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productArgumentCaptor.capture());

        Product actual = productArgumentCaptor.getValue();

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("currency.id")
                .isEqualTo(expected);

        ProductResponse expectedResponse = new ProductResponse(
                expected.getId(),
                expected.getName(),
                expected.getDescription(),
                expected.getPrice(),
                expected.getCurrency().getCurrency()
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @Test
    void givenInvalidProductId_whenUpdateProduct_thenThrowException() {
        // given
        Integer productId = 1;

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        ProductRequest productRequest = new ProductRequest(
                "Product 1",
                null,
                null,
                "EUR"
        );

        // when
        // then
        assertThatThrownBy(() -> productService.updateProduct(productId, productRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }
}