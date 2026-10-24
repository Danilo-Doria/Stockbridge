package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.InvalidProductException;

import java.math.BigDecimal;

public class Product {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int stock;
    private boolean active;

    private Product() {
    }

    public static Product create(String code, String name, String description,
                                 String category, BigDecimal price, int stock) {
        return build(null, code, name, description, category, price, stock, true);
    }

    public static Product restore(Long id, String code, String name, String description,
                                  String category, BigDecimal price, int stock, boolean active) {
        return build(id, code, name, description, category, price, stock, active);
    }

    private static Product build(Long id, String code, String name, String description,
                                 String category, BigDecimal price, int stock, boolean active) {
        requireText(code, "El código es obligatorio");
        requireText(name, "El nombre es obligatorio");
        requireText(category, "La categoría es obligatoria");
        requirePositivePrice(price);
        requireNonNegativeStock(stock);

        Product p = new Product();
        p.id = id;
        p.code = code.trim();
        p.name = name.trim();
        p.description = description;
        p.category = category.trim();
        p.price = price;
        p.stock = stock;
        p.active = active;
        return p;
    }

    public void update(String name, String description, String category, BigDecimal price) {
        requireText(name, "El nombre es obligatorio");
        requireText(category, "La categoría es obligatoria");
        requirePositivePrice(price);
        this.name = name.trim();
        this.description = description;
        this.category = category.trim();
        this.price = price;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean hasEnoughStock(int quantity) {
        return stock >= quantity;
    }

    public void decreaseStock(int quantity) {
        requirePositiveQuantity(quantity);
        if (!hasEnoughStock(quantity)) {
            throw new InsufficientStockException(
                    "Stock insuficiente para el producto " + code
                            + ": disponible " + stock + ", solicitado " + quantity);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        requirePositiveQuantity(quantity);
        this.stock += quantity;
    }

    public boolean isBelowMinimum(int minimum) {
        return stock < minimum;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException(message);
        }
    }

    private static void requirePositivePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductException("El precio debe ser mayor que cero");
        }
    }

    private static void requireNonNegativeStock(int stock) {
        if (stock < 0) {
            throw new InvalidProductException("El stock no puede ser negativo");
        }
    }

    private static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidProductException("La cantidad debe ser mayor que cero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public boolean isActive() {
        return active;
    }
}
