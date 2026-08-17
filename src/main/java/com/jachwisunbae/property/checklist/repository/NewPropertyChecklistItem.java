package com.jachwisunbae.property.checklist.repository;

import com.jachwisunbae.property.checklist.type.CheckStatus;

public record NewPropertyChecklistItem(
        Long sourceSystemCheckItemId,
        String question,
        String guide,
        int displayOrder,
        CheckStatus status,
        String memo) {
}
