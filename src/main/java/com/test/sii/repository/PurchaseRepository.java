package com.test.sii.repository;

import com.test.sii.dto.SalesReportEntryResponse;
import com.test.sii.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {

    @Query("select new com.test.sii.dto.SalesReportEntryResponse(\n" +
           "c.currency, \n" +
           "sum(p.regularPrice), \n" +
           "sum(p.discountAmount), \n" +
           "count(*))\n" +
           "from Purchase p \n" +
           "inner join Product pr on p.product.id = pr.id \n" +
           "inner join Currency c on pr.currency.id = c.id \n" +
           "group by c.currency")
    List<SalesReportEntryResponse> generateSalesReport();
}
