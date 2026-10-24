package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.domain.model.Request;
import com.stockbridge.inventory.domain.model.RequestStatus;

import java.util.Optional;

public interface RequestRepositoryPort {
    Request save(Request request);
    Optional<Request> findById(Long id);
    Optional<RequestView> findViewById(Long id);
    PageResult<RequestView> findViewsByUser(Long userId, int page, int size);
    PageResult<RequestView> findViews(RequestStatus status, int page, int size);   // status puede ser null
}
