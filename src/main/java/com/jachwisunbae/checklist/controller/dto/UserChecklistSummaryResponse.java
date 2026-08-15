package com.jachwisunbae.checklist.controller.dto;

import com.jachwisunbae.checklist.service.dto.UserChecklistSummaryResult;
import com.jachwisunbae.checklist.type.Stage;

public record UserChecklistSummaryResponse(
        Long checklistId,
        String name,
        Stage stage,
        int itemCount,
        int appliedPropertyCount) {

    public static UserChecklistSummaryResponse from(UserChecklistSummaryResult result) {
        return new UserChecklistSummaryResponse(
                result.checklistId(), result.name(), result.stage(),
                result.itemCount(), result.appliedPropertyCount());
    }
}
