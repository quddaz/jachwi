package com.jachwisunbae.property.controller.dto;

import java.time.LocalDateTime;

import com.jachwisunbae.property.service.dto.PropertyResult;

public record PropertyResponse(
        Long propertyId,
        String name,
        Long depositAmount,
        Long monthlyRentAmount,
        Long maintenanceFeeAmount,
        String address,
        String discoverySource,
        LocalDateTime lastActivityAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ProgressResponse progress) {

    public static PropertyResponse from(PropertyResult result) {
        return new PropertyResponse(
                result.propertyId(), result.name(), result.depositAmount(),
                result.monthlyRentAmount(), result.maintenanceFeeAmount(), result.address(),
                result.discoverySource(), result.lastActivityAt(), result.createdAt(),
                result.updatedAt(), new ProgressResponse(
                        result.progress().totalCount(), result.progress().completedCount(),
                        result.progress().goodCount(), result.progress().cautionCount(),
                        result.progress().unconfirmedCount(),
                        result.progress().progressPercent()));
    }

    public record ProgressResponse(
            long totalCount,
            long completedCount,
            long goodCount,
            long cautionCount,
            long unconfirmedCount,
            int progressPercent) {
    }
}
