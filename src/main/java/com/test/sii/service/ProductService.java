package com.test.sii.service;

import com.test.sii.dto.ProductCreateRequest;
import com.test.sii.dto.ProductRequest;
import com.test.sii.dto.ProductResponse;
import com.test.sii.dto.ProductUpdateRequest;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class ProductService {

    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;


    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll().stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency().getCurrency()
                        ))
                .toList();
    }

    public ProductResponse createProduct(@Valid ProductCreateRequest productRequest) {

        Product product = new Product();
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());

        Currency currency = currencyRepository.findByCurrency(productRequest.currency())
                .orElse(new Currency(null, productRequest.currency()));
        product.setCurrency(currency);

        try {
            product = productRepository.save(product);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getCurrency().getCurrency());
    }


    public ProductResponse updateProduct(Integer id, ProductRequest productRequest) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found", new Exception("Product with id " + id + " was not found")));

        Currency currency = currencyRepository.findByCurrency(productRequest.currency())
                .orElse(new Currency(null, productRequest.currency()));

        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                currency

        );
        product.updateFieldsByRequest(productUpdateRequest);

        product = productRepository.save(product);

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );
    }
}
