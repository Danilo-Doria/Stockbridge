package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InvalidMovementException;

import java.time.LocalDateTime;

public class InventoryMovement {

    private final Long id;
    private final Long productId;
    private final Long requestId;      // null si es una entrada manual
    private final Long performedBy;     // userId de bodega/admin
    private final MovementType type;
    private final int quantity;
    private final LocalDateTime createdAt;

    private InventoryMovement(Long id, Long productId, Long requestId, Long performedBy,
                              MovementType type, int quantity, LocalDateTime createdAt) {
        if (productId == null)  throw new InvalidMovementException("El producto es obligatorio");
        if (performedBy == null) throw new InvalidMovementException("El responsable es obligatorio");
        if (type == null)       throw new InvalidMovementException("El tipo es obligatorio");
        if (quantity <= 0)      throw new InvalidMovementException("La cantidad debe ser mayor que cero");
        this.id = id;
        this.productId = productId;
        this.requestId = requestId;
        this.performedBy = performedBy;
        this.type = type;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public static InventoryMovement outbound(Long productId, Long requestId, Long performedBy, int quantity) {
        return new InventoryMovement(null, productId, requestId, performedBy,
                MovementType.SALIDA, quantity, LocalDateTime.now());
    }

    public static InventoryMovement inbound(Long productId, Long performedBy, int quantity) {
        return new InventoryMovement(null, productId, null, performedBy,
                MovementType.ENTRADA, quantity, LocalDateTime.now());
    }

    public static InventoryMovement restore(Long id, Long productId, Long requestId, Long performedBy,
                                            MovementType type, int quantity, LocalDateTime createdAt) {
        if (id == null) throw new InvalidMovementException("El id es obligatorio al reconstruir");
        return new InventoryMovement(id, productId, requestId, performedBy, type, quantity, createdAt);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPerformedBy() {
        return performedBy;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Long getRequestId() {
        return requestId;
    }

    public MovementType getType() {
        return type;
    }
}
