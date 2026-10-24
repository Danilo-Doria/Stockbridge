package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.domain.model.InventoryMovement;

public interface ListMovementsUseCase {
    PageResult<InventoryMovement> byProduct(Long productId, int page, int size);
}
