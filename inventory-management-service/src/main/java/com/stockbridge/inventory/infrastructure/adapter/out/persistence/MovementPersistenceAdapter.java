package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class MovementPersistenceAdapter implements MovementRepositoryPort {

    private final SpringDataMovementRepository repository;
    private final SpringDataProductRepository products;
    private final SpringDataRequestRepository requests;
    private final SpringDataUserRepository users;

    public MovementPersistenceAdapter(
            SpringDataMovementRepository repository,
            SpringDataProductRepository products,
            SpringDataRequestRepository requests,
            SpringDataUserRepository users) {
        this.repository = repository;
        this.products = products;
        this.requests = requests;
        this.users = users;
    }

    @Override
    public InventoryMovement save(InventoryMovement m) {
        InventoryMovementEntity e = new InventoryMovementEntity();
        e.setProduct(products.getReferenceById(m.getProductId()));
        e.setRequest(m.getRequestId() == null ? null : requests.getReferenceById(m.getRequestId()));
        e.setPerformedBy(users.getReferenceById(m.getPerformedBy()));
        e.setType(m.getType());
        e.setQuantity(m.getQuantity());
        e.setCreatedAt(m.getCreatedAt());
        return toDomain(repository.save(e));
    }

    @Override
    public PageResult<InventoryMovement> findByProductId(Long productId, int page, int size) {
        Page<InventoryMovementEntity> p = repository.findByProduct_Id(productId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(p.getContent().stream().map(this::toDomain).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements());
    }

    private InventoryMovement toDomain(InventoryMovementEntity e) {
        return InventoryMovement.restore(e.getId(), e.getProduct().getId(),
                e.getRequest() == null ? null : e.getRequest().getId(),
                e.getPerformedBy().getId(), e.getType(), e.getQuantity(), e.getCreatedAt());
    }
}
