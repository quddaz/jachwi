package com.jachwisunbae.checklist.service.dto;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;

public record SystemCheckItemResult(
        Long checkItemId,
        Stage stage,
        ItemType itemType,
        String question,
        String guide) {

    public static SystemCheckItemResult from(SystemCheckItem item) {
        return new SystemCheckItemResult(
                item.getId(),
                item.getStage(),
                item.getItemType(),
                item.getQuestion(),
                item.getGuide());
    }
}
