package com.jachwisunbae.property.service.validation;

import org.springframework.stereotype.Component;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.service.dto.CreatePropertyCommand;
import com.jachwisunbae.property.service.dto.UpdatePropertyCommand;

@Component
public class PropertyValidator {

    public String validate(CreatePropertyCommand command) {
        String name = normalizeName(command.name());
        validateAmount(command.depositAmount());
        validateAmount(command.monthlyRentAmount());
        validateAmount(command.maintenanceFeeAmount());
        validateLength(command.address(), 500);
        validateLength(command.discoverySource(), 500);
        return name;
    }

    public void validate(UpdatePropertyCommand command) {
        if (!command.hasAnyField()) {
            throw invalid("수정할 매물 필드가 없습니다.");
        }
        if (command.name().present()) {
            normalizeName(command.name().value());
        }
        validatePresentAmount(command.depositAmount().present(), command.depositAmount().value());
        validatePresentAmount(
                command.monthlyRentAmount().present(), command.monthlyRentAmount().value());
        validatePresentAmount(
                command.maintenanceFeeAmount().present(), command.maintenanceFeeAmount().value());
        if (command.address().present()) {
            validateLength(command.address().value(), 500);
        }
        if (command.discoverySource().present()) {
            validateLength(command.discoverySource().value(), 500);
        }
    }

    public String normalizeName(String name) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 50) {
            throw invalid("매물 이름은 1자 이상 50자 이하여야 합니다.");
        }
        return name.trim();
    }

    public String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        String normalized = query.trim();
        if (normalized.length() > 50) {
            throw invalid("검색어는 50자 이하여야 합니다.");
        }
        return normalized;
    }

    public void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw invalid("페이지는 0 이상, 크기는 1 이상 50 이하여야 합니다.");
        }
    }

    private void validatePresentAmount(boolean present, Long amount) {
        if (present) {
            validateAmount(amount);
        }
    }

    private void validateAmount(Long amount) {
        if (amount != null && amount < 0) {
            throw invalid("금액은 0 이상이어야 합니다.");
        }
    }

    private void validateLength(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw invalid("문자열 길이 제한을 초과했습니다.");
        }
    }

    private BusinessException invalid(String message) {
        return new BusinessException(DomainErrorCode.PROPERTY_INPUT_INVALID, message);
    }
}
