package com.jachwisunbae.property.service.dto;

public record UpdatePropertyCommand(
        FieldUpdate<String> name,
        FieldUpdate<Long> depositAmount,
        FieldUpdate<Long> monthlyRentAmount,
        FieldUpdate<Long> maintenanceFeeAmount,
        FieldUpdate<String> address,
        FieldUpdate<String> discoverySource) {

    public boolean hasAnyField() {
        return name.present()
                || depositAmount.present()
                || monthlyRentAmount.present()
                || maintenanceFeeAmount.present()
                || address.present()
                || discoverySource.present();
    }
}
