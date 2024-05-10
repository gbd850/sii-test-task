package com.test.sii.repository;

import com.test.sii.model.PromoCodeMonetary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoCodeMonetaryRepository extends JpaRepository<PromoCodeMonetary, Integer> {
    Optional<PromoCodeMonetary> findByCode(String code);
}
