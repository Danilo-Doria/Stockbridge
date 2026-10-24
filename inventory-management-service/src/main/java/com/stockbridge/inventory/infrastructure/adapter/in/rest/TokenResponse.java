package com.stockbridge.inventory.infrastructure.adapter.in.rest;

public record TokenResponse(String token, String type, long expiresInSeconds) {}

