package com.jachwisunbae.property.checklist.service.dto;

import com.jachwisunbae.property.checklist.entity.PropertyChecklistItem;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public record PropertyChecklistItemResult(
        Long itemId,
        Long sourceCheckItemId,
        String question,
        String guide,
        int displayOrder,
        CheckStatus status,
        String memo) {

    public static PropertyChecklistItemResult from(PropertyChecklistItem item) {
        return new PropertyChecklistItemResult(
                item.getId(), item.getSourceSystemCheckItemId(), item.getQuestion(),
                item.getGuide(), item.getDisplayOrder(), item.getStatus(), item.getMemo());
    }
}
