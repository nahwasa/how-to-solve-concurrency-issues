package com.nahwasa.study.howtosolveconcurrencyissues.repository;

import com.nahwasa.study.howtosolveconcurrencyissues.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
