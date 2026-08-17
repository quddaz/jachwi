package com.jachwisunbae.property.checklist.entity;

import com.jachwisunbae.property.checklist.type.CheckStatus;

import lombok.Getter;

@Getter
public class PropertyChecklistItem {

    private final Long id;
    private final Long propertyChecklistId;
    private final Long sourceSystemCheckItemId;
    private final String question;
    private final String guide;
    private final int displayOrder;
    private final CheckStatus status;
    private final String memo;

    private PropertyChecklistItem(
            Long id,
            Long propertyChecklistId,
            Long sourceSystemCheckItemId,
            String question,
            String guide,
            int displayOrder,
            CheckStatus status,
            String memo) {
        this.id = id;
        this.propertyChecklistId = propertyChecklistId;
        this.sourceSystemCheckItemId = sourceSystemCheckItemId;
        this.question = question;
        this.guide = guide;
        this.displayOrder = displayOrder;
        this.status = status;
        this.memo = memo;
    }

    public static PropertyChecklistItem restore(
            Long id,
            Long propertyChecklistId,
            Long sourceSystemCheckItemId,
            String question,
            String guide,
            int displayOrder,
            CheckStatus status,
            String memo) {
        return new PropertyChecklistItem(
                id, propertyChecklistId, sourceSystemCheckItemId, question, guide,
                displayOrder, status, memo);
    }
}
