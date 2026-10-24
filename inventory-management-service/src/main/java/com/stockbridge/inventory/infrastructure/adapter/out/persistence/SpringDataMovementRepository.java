package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMovementRepository extends JpaRepository<InventoryMovementEntity, Long> {
    Page<InventoryMovementEntity> findByProduct_Id(Long productId, Pageable pageable);
}
