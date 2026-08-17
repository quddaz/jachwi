package com.jachwisunbae.property.service.policy;

import org.springframework.stereotype.Component;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

@Component
public class PropertyPolicy {

    private static final int MAX_PROPERTIES = 50;

    public void validateCreationAllowed(long currentCount) {
        if (currentCount >= MAX_PROPERTIES) {
            throw new BusinessException(
                    DomainErrorCode.PROPERTY_LIMIT_EXCEEDED,
                    "회원당 매물은 최대 50개까지 등록할 수 있습니다.");
        }
    }
}
