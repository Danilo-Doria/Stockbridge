package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.domain.model.Role;

public interface ManageRequestUseCase {
    RequestView approve(Long id);
    RequestView reject(Long id, String reason);
    RequestView cancel(Long id, Long requesterId, Role requesterRole);
    RequestView deliver(Long id, Long performedBy);
}
