package com.example.inventory.exception;

import java.time.Instant;
import java.util.Map;

/** The JSON shape of every error response. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
