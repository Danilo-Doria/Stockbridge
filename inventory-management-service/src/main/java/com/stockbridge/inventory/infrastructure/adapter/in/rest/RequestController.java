package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.application.port.in.CreateRequestUseCase;
import com.stockbridge.inventory.application.port.in.GetRequestUseCase;
import com.stockbridge.inventory.application.port.in.ListRequestsUseCase;
import com.stockbridge.inventory.application.port.in.ManageRequestUseCase;
import com.stockbridge.inventory.domain.model.RequestStatus;
import com.stockbridge.inventory.infrastructure.config.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final CreateRequestUseCase create;
    private final GetRequestUseCase get;
    private final ListRequestsUseCase list;
    private final ManageRequestUseCase manage;

    public RequestController(CreateRequestUseCase create, GetRequestUseCase get,
                             ListRequestsUseCase list, ManageRequestUseCase manage) {
        this.create = create; this.get = get; this.list = list; this.manage = manage;
    }

    @PostMapping
    public ResponseEntity<RequestResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @Valid @RequestBody CreateRequestRequest r) {
        RequestView v = create.create(new CreateRequestUseCase.CreateRequestCommand(
                user.id(), r.productId(), r.quantity(), r.observation()));
        return ResponseEntity.created(URI.create("/api/requests/" + v.request().getId()))
                .body(RequestResponse.from(v));
    }

    @GetMapping("/mine")
    public PageResult<RequestResponse> mine(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return list.listMine(user.id(), page, size).map(RequestResponse::from);
    }

    @GetMapping
    public PageResult<RequestResponse> all(@RequestParam(required = false) RequestStatus status,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return list.listAll(status, page, size).map(RequestResponse::from);
    }

    @GetMapping("/{id}")
    public RequestResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return RequestResponse.from(get.getById(id, user.id(), user.role()));
    }

    @PatchMapping("/{id}/approve")
    public RequestResponse approve(@PathVariable Long id) {
        return RequestResponse.from(manage.approve(id));
    }

    @PatchMapping("/{id}/reject")
    public RequestResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequestRequest r) {
        return RequestResponse.from(manage.reject(id, r.reason()));
    }

    @PatchMapping("/{id}/cancel")
    public RequestResponse cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return RequestResponse.from(manage.cancel(id, user.id(), user.role()));
    }

    @PatchMapping("/{id}/deliver")
    public RequestResponse deliver(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return RequestResponse.from(manage.deliver(id, user.id()));
    }
}
