package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsByCode(String code);

    @Query("select p from ProductEntity p where p.stock < :minimum")
    Page<ProductEntity> findLowStock(@Param("minimum") int minimum, Pageable pageable);
}
