package com.jachwisunbae.property.checklist.service.validation;

import org.springframework.stereotype.Component;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.checklist.type.CheckStatus;

@Component
public class AppliedChecklistValidator {

    public void validateStage(Stage requestedStage, Stage sourceStage) {
        if (requestedStage == null || requestedStage != sourceStage) {
            throw new BusinessException(
                    DomainErrorCode.PROPERTY_CHECKLIST_STAGE_MISMATCH,
                    "요청 단계와 원본 체크리스트 단계가 다릅니다.");
        }
    }

    public void validateStatus(CheckStatus status) {
        if (status == null) {
            throw new BusinessException(
                    DomainErrorCode.PROPERTY_CHECK_RESULT_INVALID,
                    "체크 상태는 필수입니다.");
        }
    }

    public String validateMemo(String memo) {
        if (memo == null || memo.length() > 500) {
            throw new BusinessException(
                    DomainErrorCode.PROPERTY_CHECK_RESULT_INVALID,
                    "체크 메모는 500자 이하여야 합니다.");
        }
        return memo;
    }
}
