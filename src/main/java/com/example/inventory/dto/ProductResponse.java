package com.example.inventory.dto;

import com.example.inventory.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;

/** Data the API returns. The entity is never exposed directly. */
public record ProductResponse(
        Long id,
        String name,
        String description,
        int quantity,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(),
                p.getQuantity(), p.getPrice(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
