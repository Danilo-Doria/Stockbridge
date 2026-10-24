package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.in.*;
import com.stockbridge.inventory.domain.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase create;
    private final UpdateProductUseCase update;
    private final GetProductUseCase get;
    private final ListProductsUseCase list;
    private final ChangeProductStatusUseCase status;

    public ProductController(CreateProductUseCase create, UpdateProductUseCase update,
                             GetProductUseCase get, ListProductsUseCase list,
                             ChangeProductStatusUseCase status) {
        this.create = create;
        this.update = update;
        this.get = get;
        this.list = list;
        this.status = status;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest r) {
        Product p = create.create(new CreateProductUseCase.CreateProductCommand(
                r.code(), r.name(), r.description(), r.category(), r.price(), r.stock()));
        return ResponseEntity.created(URI.create("/api/products/" + p.getId()))
                .body(ProductResponse.from(p));
    }

    @GetMapping
    public PageResult<ProductResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return list.list(page, size).map(ProductResponse::from);
    }

    @GetMapping("/low-stock")
    public PageResult<ProductResponse> lowStock(@RequestParam int minimum,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return list.listLowStock(minimum, page, size).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return ProductResponse.from(get.getById(id));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest r) {
        return ProductResponse.from(update.update(id,
                new UpdateProductUseCase.UpdateProductCommand(r.name(), r.description(), r.category(), r.price())));
    }

    @PatchMapping("/{id}/activate")
    public ProductResponse activate(@PathVariable Long id) {
        return ProductResponse.from(status.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ProductResponse deactivate(@PathVariable Long id) {
        return ProductResponse.from(status.deactivate(id));
    }
}
