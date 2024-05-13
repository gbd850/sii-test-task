package com.test.sii.service;

import com.test.sii.dto.ProductCreateRequest;
import com.test.sii.dto.ProductRequest;
import com.test.sii.dto.ProductResponse;
import com.test.sii.dto.ProductUpdateRequest;
import com.test.sii.model.Currency;
import com.test.sii.model.Product;
import com.test.sii.repository.CurrencyRepository;
import com.test.sii.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class ProductService {

    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;


    public List<ProductResponse> getAllProducts(Integer page, Integer size) {
        if (page != null && size == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be specified", new Exception("Page size cannot be null when providing page number"));
        }

        if (page != null && page <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number must not be less than zero", new Exception("Page number cannot be " + page));
        }

        List<Product> result = page != null ? productRepository.findAll(PageRequest.of(page - 1, size)).getContent() : productRepository.findAll();

        return result.stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency().getCurrency()
                        ))
                .toList();
    }

    private Currency getCurrency(String currency) {
        Currency curr = currencyRepository.findByCurrency(currency).orElse(null);
        if (curr == null) {
            curr = currencyRepository.save(new Currency(null, currency));
        }
        return curr;
    }

    private Product getProduct(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found", new Exception("Product with id " + id + " was not found")));
    }

    @Transactional
    public ProductResponse createProduct(@Valid ProductCreateRequest productRequest) {

        Product product = new Product();
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());

        Currency currency = getCurrency(productRequest.currency());
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

    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest productRequest) {

        Product product = getProduct(id);

        Currency currency = getCurrency(productRequest.currency());

        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                currency

        );
        product.updateFieldsByRequest(productUpdateRequest);

        try {
            product = productRepository.save(product);
        }
        catch (OptimisticLockException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, e.getMessage());
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().getCurrency()
        );
    }

}
