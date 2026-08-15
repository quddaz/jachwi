package com.jachwisunbae.checklist.service.dto;

import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.type.Stage;

public record UserChecklistSummaryResult(
        Long checklistId,
        String name,
        Stage stage,
        int itemCount,
        int appliedPropertyCount) {

    public static UserChecklistSummaryResult from(
            UserChecklist checklist,
            int itemCount,
            int appliedPropertyCount) {
        return new UserChecklistSummaryResult(
                checklist.getId(),
                checklist.getName(),
                checklist.getStage(),
                itemCount,
                appliedPropertyCount);
    }
}
