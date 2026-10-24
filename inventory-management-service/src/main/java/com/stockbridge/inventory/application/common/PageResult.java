package com.stockbridge.inventory.application.common;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {
    public <R> PageResult<R> map(Function<T, R> fn) {
        return new PageResult<>(content.stream().map(fn).toList(), page, size, totalElements);
    }
}
