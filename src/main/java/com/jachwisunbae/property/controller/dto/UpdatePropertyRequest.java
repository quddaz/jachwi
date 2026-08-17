package com.jachwisunbae.property.controller.dto;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.service.dto.FieldUpdate;
import com.jachwisunbae.property.service.dto.UpdatePropertyCommand;

import tools.jackson.databind.JsonNode;

public record UpdatePropertyRequest(
        JsonNode name,
        JsonNode depositAmount,
        JsonNode monthlyRentAmount,
        JsonNode maintenanceFeeAmount,
        JsonNode address,
        JsonNode discoverySource) {

    public UpdatePropertyCommand toCommand() {
        return new UpdatePropertyCommand(
                stringField(name),
                longField(depositAmount),
                longField(monthlyRentAmount),
                longField(maintenanceFeeAmount),
                stringField(address),
                stringField(discoverySource));
    }

    private FieldUpdate<String> stringField(JsonNode node) {
        if (node == null) {
            return FieldUpdate.omitted();
        }
        if (node.isNull()) {
            return FieldUpdate.of(null);
        }
        if (!node.isString()) {
            throw invalidType();
        }
        return FieldUpdate.of(node.stringValue());
    }

    private FieldUpdate<Long> longField(JsonNode node) {
        if (node == null) {
            return FieldUpdate.omitted();
        }
        if (node.isNull()) {
            return FieldUpdate.of(null);
        }
        if (!node.isIntegralNumber() || !node.canConvertToLong()) {
            throw invalidType();
        }
        return FieldUpdate.of(node.longValue());
    }

    private BusinessException invalidType() {
        return new BusinessException(
                DomainErrorCode.PROPERTY_INPUT_INVALID,
                "매물 수정 필드 타입이 올바르지 않습니다.");
    }
}
