package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.in.*;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.domain.exception.DuplicateProductCodeException;
import com.stockbridge.inventory.domain.exception.ProductNotFoundException;
import com.stockbridge.inventory.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService implements CreateProductUseCase, UpdateProductUseCase,
        GetProductUseCase, ListProductsUseCase, ChangeProductStatusUseCase {

    private final ProductRepositoryPort repository;

    public ProductService(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Product create(CreateProductCommand c) {
        Product product = Product.create(c.code(), c.name(), c.description(), c.category(), c.price(), c.stock());
        if (repository.existsByCode(product.getCode())) {
            throw new DuplicateProductCodeException(product.getCode());
        }
        return repository.save(product);
    }

    @Override
    public Product update(Long id, UpdateProductCommand c) {
        Product product = find(id);
        product.update(c.name(), c.description(), c.category(), c.price());
        return repository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(Long id) { return find(id); }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> list(int page, int size) { return repository.findAll(page, size); }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> listLowStock(int minimum, int page, int size) {
        return repository.findByStockLessThan(minimum, page, size);
    }

    @Override
    public Product activate(Long id) {
        Product product = find(id);
        product.activate();
        return repository.save(product);
    }

    @Override
    public Product deactivate(Long id) {
        Product product = find(id);
        product.deactivate();
        return repository.save(product);
    }

    private Product find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
