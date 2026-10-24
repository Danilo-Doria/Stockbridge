package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.domain.model.Role;

public interface GetRequestUseCase {
    RequestView getById(Long id, Long requesterId, Role requesterRole);
}
