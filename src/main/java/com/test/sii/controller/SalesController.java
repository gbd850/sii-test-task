package com.test.sii.controller;

import com.test.sii.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @GetMapping("report")
    public ResponseEntity<Object> getSalesReport() {
        return new ResponseEntity<>(salesService.getSalesReport(), HttpStatus.OK);
    }
}
