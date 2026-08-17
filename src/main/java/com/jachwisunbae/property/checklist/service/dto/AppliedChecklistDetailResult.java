package com.jachwisunbae.property.checklist.service.dto;

import java.util.List;
import java.util.ArrayList;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.entity.AppliedChecklist;
import com.jachwisunbae.property.checklist.entity.AppliedChecklistItem;

public record AppliedChecklistDetailResult(
        Long propertyChecklistId,
        Long sourceChecklistId,
        String name,
        Stage stage,
        List<AppliedChecklistItemResult> items,
        CheckProgressResult progress) {

    public static AppliedChecklistDetailResult from(
            AppliedChecklist checklist,
            List<AppliedChecklistItem> items) {
        return new AppliedChecklistDetailResult(
                checklist.getId(), checklist.getSourceUserChecklistId(), checklist.getName(),
                checklist.getStage(), toItemResults(items),
                CheckProgressResult.from(items));
    }

    private static List<AppliedChecklistItemResult> toItemResults(
            List<AppliedChecklistItem> items) {
        List<AppliedChecklistItemResult> results = new ArrayList<>();
        for (AppliedChecklistItem item : items) {
            results.add(AppliedChecklistItemResult.from(item));
        }
        return List.copyOf(results);
    }
}
