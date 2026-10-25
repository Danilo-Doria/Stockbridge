package com.stockbridge.supplier.controller;

import com.stockbridge.supplier.dto.AvailabilityRequest;
import com.stockbridge.supplier.dto.AvailabilityResponse;
import com.stockbridge.supplier.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final AvailabilityService service;

    public SupplierController(AvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check-availability")
    public AvailabilityResponse check(@Valid @RequestBody AvailabilityRequest request) {
        return service.check(request);
    }
}
