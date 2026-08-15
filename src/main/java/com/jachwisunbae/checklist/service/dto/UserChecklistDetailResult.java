package com.jachwisunbae.checklist.service.dto;

import java.util.List;
import java.util.Map;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.type.Stage;

public record UserChecklistDetailResult(
        Long checklistId,
        String name,
        Stage stage,
        List<UserChecklistItemResult> items) {

    public static UserChecklistDetailResult from(
            UserChecklist checklist,
            List<UserChecklistItem> items,
            Map<Long, SystemCheckItem> systemItemsById) {
        return new UserChecklistDetailResult(
                checklist.getId(),
                checklist.getName(),
                checklist.getStage(),
                items.stream()
                        .map(item -> UserChecklistItemResult.from(
                                item,
                                systemItemsById.get(item.getSystemCheckItemId())))
                        .toList());
    }
}
