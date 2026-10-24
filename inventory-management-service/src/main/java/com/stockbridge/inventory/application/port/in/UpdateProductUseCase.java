package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.Product;

import java.math.BigDecimal;

public interface UpdateProductUseCase {
    Product update(Long id, UpdateProductCommand command);

    record UpdateProductCommand(
            String name,
            String description,
            String category,
            BigDecimal price) {
    }
}
