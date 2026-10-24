package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.Product;

import java.math.BigDecimal;

public interface CreateProductUseCase {
    Product create(CreateProductCommand command);

    record CreateProductCommand(
            String code,
            String name,
            String description,
            String category,
            BigDecimal price,
            int stock) {
    }
}
