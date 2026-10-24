package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.domain.model.MovementType;

import java.time.LocalDateTime;

public record MovementResponse(
        Long id,
        Long productId,
        Long requestId,
        Long performedBy,
        MovementType type,
        int quantity,
        LocalDateTime createdAt) {
    public static MovementResponse from(InventoryMovement m) {
        return new MovementResponse(m.getId(), m.getProductId(), m.getRequestId(),
                m.getPerformedBy(), m.getType(), m.getQuantity(), m.getCreatedAt());
    }
}
