package com.jachwisunbae.checklist.service.dto;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.type.ItemType;

public record UserChecklistItemResult(
        Long checklistItemId,
        Long checkItemId,
        ItemType type,
        String question,
        String guide,
        int displayOrder) {

    public static UserChecklistItemResult from(UserChecklistItem item, SystemCheckItem systemItem) {
        return new UserChecklistItemResult(
                item.getId(),
                item.getSystemCheckItemId(),
                systemItem.getItemType(),
                systemItem.getQuestion(),
                systemItem.getGuide(),
                item.getDisplayOrder());
    }
}
