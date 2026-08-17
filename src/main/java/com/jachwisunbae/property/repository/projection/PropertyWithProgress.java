package com.jachwisunbae.property.repository.projection;

import com.jachwisunbae.property.entity.Property;

public record PropertyWithProgress(
        Property property,
        long totalCount,
        long completedCount,
        long goodCount,
        long cautionCount,
        long unconfirmedCount) {
}
