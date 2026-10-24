package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.domain.model.Product;

public interface ListProductsUseCase {
    PageResult<Product> list(int page, int size);

    PageResult<Product> listLowStock(int minimum, int page, int size);
}
