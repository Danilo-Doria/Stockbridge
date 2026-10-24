package com.stockbridge.inventory.application.common;

public record AccessToken(String value, long expiresInSeconds) {}