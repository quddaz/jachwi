package com.jachwisunbae.property.checklist.controller.dto;

import java.util.List;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.service.dto.PropertyChecklistDetailResult;
import com.jachwisunbae.property.checklist.service.dto.PropertyChecklistItemResult;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public record PropertyChecklistDetailResponse(
        Long propertyChecklistId,
        Long sourceChecklistId,
        String name,
        Stage stage,
        List<ItemResponse> items,
        CheckProgressResponse progress) {

    public static PropertyChecklistDetailResponse from(PropertyChecklistDetailResult result) {
        return new PropertyChecklistDetailResponse(
                result.propertyChecklistId(), result.sourceChecklistId(), result.name(),
                result.stage(), result.items().stream().map(ItemResponse::from).toList(),
                CheckProgressResponse.from(result.progress()));
    }

    public record ItemResponse(
            Long itemId,
            Long sourceCheckItemId,
            String question,
            String guide,
            int displayOrder,
            CheckStatus status,
            String memo) {

        private static ItemResponse from(PropertyChecklistItemResult result) {
            return new ItemResponse(
                    result.itemId(), result.sourceCheckItemId(), result.question(), result.guide(),
                    result.displayOrder(), result.status(), result.memo());
        }
    }
}
