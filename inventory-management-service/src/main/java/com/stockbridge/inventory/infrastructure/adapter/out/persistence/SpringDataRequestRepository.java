package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.domain.model.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRequestRepository extends JpaRepository<RequestEntity, Long> {

    @EntityGraph(attributePaths = {"user", "product"})
    Optional<RequestEntity> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"user", "product"})
    Page<RequestEntity> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "product"})
    Page<RequestEntity> findByStatus(RequestStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "product"})
    Page<RequestEntity> findAll(Pageable pageable);
}

