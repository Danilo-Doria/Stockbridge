package com.stockbridge.supplier.dto;

public record AvailabilityResponse(String supplierCode, boolean available,
                                   int availableQuantity, int estimatedDeliveryDays) {}
