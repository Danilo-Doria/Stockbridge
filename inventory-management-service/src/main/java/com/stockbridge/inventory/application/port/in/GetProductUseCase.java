package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.Product;

public interface GetProductUseCase {
    Product getById(Long id);
}
