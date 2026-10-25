package com.stockbridge.inventory.application.port.out;

public interface SupplierAvailabilityPort {
    SupplierAvailability check(String productCode, int quantity, String category);

    record SupplierAvailability(String supplierCode, boolean available,
                                int availableQuantity, int estimatedDeliveryDays) {}
}
