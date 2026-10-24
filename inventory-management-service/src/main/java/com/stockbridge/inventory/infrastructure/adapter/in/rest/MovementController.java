package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.in.ListMovementsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movements")
public class MovementController {
    private final ListMovementsUseCase movements;
    public MovementController(ListMovementsUseCase movements) { this.movements = movements; }

    @GetMapping
    public PageResult<MovementResponse> byProduct(@RequestParam Long productId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return movements.byProduct(productId, page, size).map(MovementResponse::from);
    }
}
