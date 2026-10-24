package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.domain.model.RequestStatus;

public interface ListRequestsUseCase {
    PageResult<RequestView> listMine(Long userId, int page, int size);
    PageResult<RequestView> listAll(RequestStatus status, int page, int size);
}
