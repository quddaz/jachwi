package com.jachwisunbae.property.service.dto;

public record CreatePropertyCommand(
        String name,
        Long depositAmount,
        Long monthlyRentAmount,
        Long maintenanceFeeAmount,
        String address,
        String discoverySource) {
}
