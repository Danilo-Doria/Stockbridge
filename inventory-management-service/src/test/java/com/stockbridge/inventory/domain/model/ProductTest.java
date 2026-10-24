package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.InvalidProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product validProduct(int stock) {
        return Product.create("MON-001", "Monitor 24\"", "Full HD",
                "MONITORES", new BigDecimal("350.00"), stock);
    }

    @Test
    @DisplayName("Crea un producto válido, activo y sin id")
    void createsValidProduct() {
        Product p = validProduct(10);

        assertNull(p.getId());
        assertEquals("MON-001", p.getCode());
        assertEquals(0, new BigDecimal("350.00").compareTo(p.getPrice()));
        assertEquals(10, p.getStock());
        assertTrue(p.isActive());
    }

    @Test
    @DisplayName("Rechaza precio cero")
    void rejectsZeroPrice() {
        assertThrows(InvalidProductException.class, () ->
                Product.create("A-1", "Item", null, "CAT", BigDecimal.ZERO, 5));
    }

    @Test
    @DisplayName("Rechaza precio negativo")
    void rejectsNegativePrice() {
        assertThrows(InvalidProductException.class, () ->
                Product.create("A-1", "Item", null, "CAT", new BigDecimal("-1"), 5));
    }

    @Test
    @DisplayName("Rechaza precio nulo")
    void rejectsNullPrice() {
        assertThrows(InvalidProductException.class, () ->
                Product.create("A-1", "Item", null, "CAT", null, 5));
    }

    @Test
    @DisplayName("Rechaza stock negativo")
    void rejectsNegativeStock() {
        assertThrows(InvalidProductException.class, () ->
                Product.create("A-1", "Item", null, "CAT", BigDecimal.TEN, -1));
    }

    @Test
    @DisplayName("Rechaza código vacío")
    void rejectsBlankCode() {
        assertThrows(InvalidProductException.class, () ->
                Product.create("  ", "Item", null, "CAT", BigDecimal.TEN, 1));
    }

    @Test
    @DisplayName("decreaseStock descuenta correctamente")
    void decreasesStock() {
        Product p = validProduct(10);

        p.decreaseStock(4);

        assertEquals(6, p.getStock());
    }

    @Test
    @DisplayName("decreaseStock permite dejar el stock exactamente en cero")
    void decreasesStockToZero() {
        Product p = validProduct(5);

        p.decreaseStock(5);

        assertEquals(0, p.getStock());
    }

    @Test
    @DisplayName("decreaseStock lanza InsufficientStockException si no alcanza y no modifica el stock")
    void failsWhenStockIsInsufficient() {
        Product p = validProduct(3);

        assertThrows(InsufficientStockException.class, () -> p.decreaseStock(4));
        assertEquals(3, p.getStock());
    }

    @Test
    @DisplayName("decreaseStock rechaza cantidad cero o negativa")
    void decreaseRejectsInvalidQuantity() {
        Product p = validProduct(10);

        assertThrows(InvalidProductException.class, () -> p.decreaseStock(0));
        assertThrows(InvalidProductException.class, () -> p.decreaseStock(-2));
    }

    @Test
    @DisplayName("increaseStock suma y rechaza cantidades inválidas")
    void increasesStock() {
        Product p = validProduct(10);

        p.increaseStock(5);

        assertEquals(15, p.getStock());
        assertThrows(InvalidProductException.class, () -> p.increaseStock(0));
    }

    @Test
    @DisplayName("activate y deactivate cambian el estado")
    void togglesActive() {
        Product p = validProduct(10);

        p.deactivate();
        assertFalse(p.isActive());

        p.activate();
        assertTrue(p.isActive());
    }

    @Test
    @DisplayName("isBelowMinimum compara contra el mínimo")
    void detectsLowStock() {
        Product p = validProduct(5);

        assertTrue(p.isBelowMinimum(10));
        assertFalse(p.isBelowMinimum(5));
        assertFalse(p.isBelowMinimum(3));
    }

    @Test
    @DisplayName("restore reconstruye un producto guardado con su id y estado")
    void restoresProduct() {
        Product p = Product.restore(7L, "MON-001", "Monitor", null,
                "MONITORES", new BigDecimal("350.00"), 10, false);

        assertEquals(7L, p.getId());
        assertFalse(p.isActive());
    }
}