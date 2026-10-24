package com.stockbridge.inventory.application.common;

import com.stockbridge.inventory.domain.model.Request;

public record RequestView(Request request, String userName, String productCode, String productName) {}
