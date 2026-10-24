package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.InvalidProductException;

import java.math.BigDecimal;

public class Product {

    private final Long id;
    private final String code;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int stock;
    private boolean active;

    private Product(Long id, String code, String name, String description,
                    String category, BigDecimal price, int stock, boolean active) {
        this.id = id;
        this.code = requireText(code, "El código es obligatorio");
        this.name = requireText(name, "El nombre es obligatorio");
        this.description = description;
        this.category = requireText(category, "La categoría es obligatoria");
        this.price = requirePositivePrice(price);
        this.stock = requireNonNegativeStock(stock);
        this.active = active;
    }

    public static Product create(String code, String name, String description,
                                 String category, BigDecimal price, int stock) {
        return new Product(null, code, name, description, category, price, stock, true);
    }

    public static Product restore(Long id, String code, String name, String description,
                                  String category, BigDecimal price, int stock, boolean active) {
        if (id == null) {
            throw new InvalidProductException("El id es obligatorio al reconstruir");
        }
        return new Product(id, code, name, description, category, price, stock, active);
    }

    public void update(String name, String description, String category, BigDecimal price) {
        this.name = requireText(name, "El nombre es obligatorio");
        this.description = description;
        this.category = requireText(category, "La categoría es obligatoria");
        this.price = requirePositivePrice(price);
    }

    public void activate()   { this.active = true; }
    public void deactivate() { this.active = false; }

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

    // ---------- Validaciones: devuelven el valor para usarlas al asignar ----------

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException(message);
        }
        return value.trim();
    }

    private static BigDecimal requirePositivePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductException("El precio debe ser mayor que cero");
        }
        return price;
    }

    private static int requireNonNegativeStock(int stock) {
        if (stock < 0) {
            throw new InvalidProductException("El stock no puede ser negativo");
        }
        return stock;
    }

    private static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidProductException("La cantidad debe ser mayor que cero");
        }
    }

    public boolean isActive() {
        return active;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}