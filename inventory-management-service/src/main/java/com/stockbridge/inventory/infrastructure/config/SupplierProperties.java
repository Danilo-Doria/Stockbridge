package com.stockbridge.inventory.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supplier")
public record SupplierProperties(String baseUrl, long connectTimeoutMs, long readTimeoutMs) {}
