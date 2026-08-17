package com.jachwisunbae.property.checklist.controller.dto;

import com.jachwisunbae.property.checklist.service.dto.AppliedChecklistItemResult;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public record PropertyChecklistItemResponse(
        Long itemId,
        Long sourceCheckItemId,
        String question,
        String guide,
        int displayOrder,
        CheckStatus status,
        String memo) {

    public static PropertyChecklistItemResponse from(AppliedChecklistItemResult result) {
        return new PropertyChecklistItemResponse(
                result.itemId(), result.sourceCheckItemId(), result.question(), result.guide(),
                result.displayOrder(), result.status(), result.memo());
    }
}
