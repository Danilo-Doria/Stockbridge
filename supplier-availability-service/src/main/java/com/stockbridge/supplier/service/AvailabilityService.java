package com.stockbridge.supplier.service;

import com.stockbridge.supplier.dto.AvailabilityRequest;
import com.stockbridge.supplier.dto.AvailabilityResponse;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    public AvailabilityResponse check(AvailabilityRequest r) {
        String key = r.productCode().trim().toUpperCase() + "|" + r.category().trim().toUpperCase();
        int h = Math.floorMod(key.hashCode(), 1000);   // String.hashCode() es determinista

        String supplierCode = "SUP-%03d".formatted(h % 5 + 1);
        int availableQuantity = 5 + h % 46;            // entre 5 y 50
        int deliveryDays = 1 + h % 7;                  // entre 1 y 7

        return new AvailabilityResponse(supplierCode, availableQuantity >= r.quantity(),
                availableQuantity, deliveryDays);
    }
}