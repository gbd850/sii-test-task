package com.test.sii.service;

import com.test.sii.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final PurchaseRepository purchaseRepository;


    public Object getSalesReport() {
//        var collect = purchaseRepository.findAll().stream()
//                .collect(
//                        Collectors.groupingBy(purchase -> purchase.getProduct().getCurrency().getCurrency(), Collectors.collectingAndThen(
//
//                        ))
//                );
        return purchaseRepository.generateSalesReport();
    }
}
