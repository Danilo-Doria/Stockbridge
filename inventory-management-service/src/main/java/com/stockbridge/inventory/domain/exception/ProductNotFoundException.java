package com.stockbridge.inventory.domain.exception;

public class ProductNotFoundException extends DomainException {
    public ProductNotFoundException(Long id) {super("Producto no encontrado: " + id); }

}
