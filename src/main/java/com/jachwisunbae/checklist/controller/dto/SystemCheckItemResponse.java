package com.jachwisunbae.checklist.controller.dto;

import com.jachwisunbae.checklist.service.dto.SystemCheckItemResult;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;

public record SystemCheckItemResponse(
        Long checkItemId,
        Stage stage,
        ItemType type,
        String question,
        String guide) {

    public static SystemCheckItemResponse from(SystemCheckItemResult result) {
        return new SystemCheckItemResponse(
                result.checkItemId(),
                result.stage(),
                result.itemType(),
                result.question(),
                result.guide());
    }
}
