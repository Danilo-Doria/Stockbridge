package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository repository;
    private final ProductPersistenceMapper mapper;

    public ProductPersistenceAdapter(SpringDataProductRepository repository,
                                     ProductPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        return mapper.toDomain(repository.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public PageResult<Product> findAll(int page, int size) {
        return toPageResult(repository.findAll(PageRequest.of(page, size, Sort.by("name"))));
    }

    @Override
    public PageResult<Product> findByStockLessThan(int minimum, int page, int size) {
        return toPageResult(repository.findLowStock(minimum, PageRequest.of(page, size, Sort.by("stock"))));
    }

    @Override
    public Optional<Product> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    private PageResult<Product> toPageResult(Page<ProductEntity> p) {
        return new PageResult<>(p.getContent().stream().map(mapper::toDomain).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements());
    }
}
