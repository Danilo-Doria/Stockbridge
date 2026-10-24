package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.application.common.RequestView;

public interface CreateRequestUseCase {
    RequestView create(CreateRequestCommand command);
    record CreateRequestCommand(Long userId, Long productId, int quantity, String observation) {}
}
