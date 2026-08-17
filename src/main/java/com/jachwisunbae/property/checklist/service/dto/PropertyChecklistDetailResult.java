package com.jachwisunbae.property.checklist.service.dto;

import java.util.List;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.entity.PropertyChecklist;
import com.jachwisunbae.property.checklist.entity.PropertyChecklistItem;

public record PropertyChecklistDetailResult(
        Long propertyChecklistId,
        Long sourceChecklistId,
        String name,
        Stage stage,
        List<PropertyChecklistItemResult> items,
        CheckProgressResult progress) {

    public static PropertyChecklistDetailResult from(
            PropertyChecklist checklist,
            List<PropertyChecklistItem> items) {
        return new PropertyChecklistDetailResult(
                checklist.getId(), checklist.getSourceUserChecklistId(), checklist.getName(),
                checklist.getStage(), items.stream().map(PropertyChecklistItemResult::from).toList(),
                CheckProgressResult.from(items));
    }
}
