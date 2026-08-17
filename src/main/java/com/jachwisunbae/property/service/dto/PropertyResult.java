package com.jachwisunbae.property.service.dto;

import java.time.LocalDateTime;

import com.jachwisunbae.property.entity.Property;

public record PropertyResult(
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
        CheckProgressResult progress) {

    public static PropertyResult from(
            Property property,
            long totalCount,
            long completedCount,
            long goodCount,
            long cautionCount,
            long unconfirmedCount) {
        return new PropertyResult(
                property.getId(), property.getName(), property.getDepositAmount(),
                property.getMonthlyRentAmount(), property.getMaintenanceFeeAmount(),
                property.getAddress(), property.getDiscoverySource(), property.getLastActivityAt(),
                property.getCreatedAt(), property.getUpdatedAt(),
                CheckProgressResult.of(
                        totalCount, completedCount, goodCount, cautionCount, unconfirmedCount));
    }
}
