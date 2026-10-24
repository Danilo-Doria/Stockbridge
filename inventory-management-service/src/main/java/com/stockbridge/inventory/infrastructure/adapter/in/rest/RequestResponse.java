package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.domain.model.Request;
import com.stockbridge.inventory.domain.model.RequestStatus;

import java.time.LocalDateTime;

public record RequestResponse(
        Long id,
        Long userId,
        String userName,
        Long productId,
        String productCode,
        String productName,
        int quantity,
        RequestStatus status,
        String observation,
        LocalDateTime createdAt) {
    public static RequestResponse from(RequestView v) {
        Request r = v.request();
        return new RequestResponse(r.getId(), r.getUserId(), v.userName(), r.getProductId(),
                v.productCode(), v.productName(), r.getQuantity(), r.getStatus(),
                r.getObservation(), r.getCreatedAt());
    }
}
