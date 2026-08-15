package com.jachwisunbae.checklist.controller.dto;

import com.jachwisunbae.checklist.service.dto.UserChecklistItemResult;
import com.jachwisunbae.checklist.type.ItemType;

public record UserChecklistItemResponse(
        Long checklistItemId,
        Long checkItemId,
        ItemType type,
        String question,
        String guide,
        int displayOrder) {

    public static UserChecklistItemResponse from(UserChecklistItemResult result) {
        return new UserChecklistItemResponse(
                result.checklistItemId(), result.checkItemId(), result.type(),
                result.question(), result.guide(), result.displayOrder());
    }
}
