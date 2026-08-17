package com.jachwisunbae.property.checklist.controller.dto;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.service.dto.AppliedChecklistSummaryResult;

public record PropertyChecklistSummaryResponse(
        Long propertyChecklistId,
        String name,
        Stage stage,
        boolean applied,
        CheckProgressResponse progress) {

    public static PropertyChecklistSummaryResponse from(AppliedChecklistSummaryResult result) {
        return new PropertyChecklistSummaryResponse(
                result.propertyChecklistId(), result.name(), result.stage(), result.applied(),
                CheckProgressResponse.from(result.progress()));
    }
}
