package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductPersistenceMapper {

    ProductEntity toEntity(Product product);

    default Product toDomain(ProductEntity e) {
        return Product.restore(e.getId(), e.getCode(), e.getName(), e.getDescription(),
                e.getCategory(), e.getPrice(), e.getStock(), e.isActive());
    }
}
