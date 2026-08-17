package com.jachwisunbae.property.checklist.service.dto;

import com.jachwisunbae.property.checklist.entity.AppliedChecklistItem;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public record AppliedChecklistItemResult(
        Long itemId,
        Long sourceCheckItemId,
        String question,
        String guide,
        int displayOrder,
        CheckStatus status,
        String memo) {

    public static AppliedChecklistItemResult from(AppliedChecklistItem item) {
        return new AppliedChecklistItemResult(
                item.getId(), item.getSourceSystemCheckItemId(), item.getQuestion(),
                item.getGuide(), item.getDisplayOrder(), item.getStatus(), item.getMemo());
    }
}
