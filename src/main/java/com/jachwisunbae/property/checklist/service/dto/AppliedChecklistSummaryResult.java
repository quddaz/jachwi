package com.jachwisunbae.property.checklist.service.dto;

import com.jachwisunbae.checklist.type.Stage;

public record AppliedChecklistSummaryResult(
        Long propertyChecklistId,
        String name,
        Stage stage,
        boolean applied,
        CheckProgressResult progress) {
}
