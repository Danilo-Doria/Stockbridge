package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.application.port.out.RequestRepositoryPort;
import com.stockbridge.inventory.domain.model.Request;
import com.stockbridge.inventory.domain.model.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestPersistenceAdapter implements RequestRepositoryPort {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final SpringDataRequestRepository repository;
    private final SpringDataUserRepository users;
    private final SpringDataProductRepository products;

    public RequestPersistenceAdapter(SpringDataRequestRepository repository,
                                     SpringDataUserRepository users,
                                     SpringDataProductRepository products) {
        this.repository = repository;
        this.users = users;
        this.products = products;
    }

    @Override
    public Request save(Request r) {
        RequestEntity e = new RequestEntity();
        e.setId(r.getId());
        e.setUser(users.getReferenceById(r.getUserId()));          // proxy: no hace SELECT
        e.setProduct(products.getReferenceById(r.getProductId()));
        e.setQuantity(r.getQuantity());
        e.setStatus(r.getStatus());
        e.setObservation(r.getObservation());
        e.setCreatedAt(r.getCreatedAt());
        return toDomain(repository.save(e));
    }

    @Override
    public Optional<Request> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<RequestView> findViewById(Long id) {
        return repository.findWithDetailsById(id).map(this::toView);
    }

    @Override
    public PageResult<RequestView> findViewsByUser(Long userId, int page, int size) {
        return toPage(repository.findByUser_Id(userId, PageRequest.of(page, size, NEWEST_FIRST)));
    }

    @Override
    public PageResult<RequestView> findViews(RequestStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);
        return toPage(status == null ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable));
    }

    private Request toDomain(RequestEntity e) {
        return Request.restore(e.getId(), e.getUser().getId(), e.getProduct().getId(),
                e.getQuantity(), e.getCreatedAt(), e.getStatus(), e.getObservation());
    }

    private RequestView toView(RequestEntity e) {
        return new RequestView(toDomain(e), e.getUser().getFullName(),
                e.getProduct().getCode(), e.getProduct().getName());
    }

    private PageResult<RequestView> toPage(Page<RequestEntity> p) {
        return new PageResult<>(p.getContent().stream().map(this::toView).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements());
    }
}
