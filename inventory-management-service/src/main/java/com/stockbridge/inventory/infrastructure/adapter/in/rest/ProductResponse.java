package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.domain.model.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String code,
        String name,
        String description,
        String category,
        BigDecimal price,
        int stock,
        boolean active) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getDescription(),
                p.getCategory(),
                p.getPrice(),
                p.getStock(),
                p.isActive());
    }
}
