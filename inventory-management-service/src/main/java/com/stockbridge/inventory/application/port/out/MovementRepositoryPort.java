package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.domain.model.InventoryMovement;

public interface MovementRepositoryPort {
    InventoryMovement save(InventoryMovement movement);
    PageResult<InventoryMovement> findByProductId(Long productId, int page, int size);
}
