package com.jachwisunbae.property.checklist.entity;

import com.jachwisunbae.property.checklist.type.CheckStatus;

public record AppliedChecklistItemDraft(
        Long sourceSystemCheckItemId,
        String question,
        String guide,
        int displayOrder,
        CheckStatus status,
        String memo) {
}
