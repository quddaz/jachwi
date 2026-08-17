package com.jachwisunbae.property.checklist.service.dto;

import com.jachwisunbae.checklist.type.Stage;

public record PropertyChecklistSummaryResult(
        Long propertyChecklistId,
        String name,
        Stage stage,
        boolean applied,
        CheckProgressResult progress) {
}
