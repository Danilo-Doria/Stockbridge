package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.domain.model.Product;

import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);

    Optional<Product> findById(Long id);

    boolean existsByCode(String code);

    PageResult<Product> findAll(int page, int size);

    PageResult<Product> findByStockLessThan(int minimum, int page, int size);
}
