package com.test.sii.service;

import com.test.sii.dto.SalesReportEntryResponse;
import com.test.sii.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final PurchaseRepository purchaseRepository;


    public List<SalesReportEntryResponse> getSalesReport() {
        return purchaseRepository.generateSalesReport();
    }
}
