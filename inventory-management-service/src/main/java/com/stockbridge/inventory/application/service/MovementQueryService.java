package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.in.ListMovementsUseCase;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MovementQueryService implements ListMovementsUseCase {
    private final MovementRepositoryPort movements;
    public MovementQueryService(MovementRepositoryPort movements) { this.movements = movements; }

    @Override
    public PageResult<InventoryMovement> byProduct(Long productId, int page, int size) {
        return movements.findByProductId(productId, page, size);
    }
}
