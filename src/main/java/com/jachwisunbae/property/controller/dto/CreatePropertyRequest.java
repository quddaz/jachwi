package com.jachwisunbae.property.controller.dto;

import com.jachwisunbae.property.service.dto.CreatePropertyCommand;

public record CreatePropertyRequest(
        String name,
        Long depositAmount,
        Long monthlyRentAmount,
        Long maintenanceFeeAmount,
        String address,
        String discoverySource) {

    public CreatePropertyCommand toCommand() {
        return new CreatePropertyCommand(
                name, depositAmount, monthlyRentAmount, maintenanceFeeAmount,
                address, discoverySource);
    }
}
