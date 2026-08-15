package com.jachwisunbae.checklist.controller.dto;

import java.util.List;

import com.jachwisunbae.checklist.service.dto.UserChecklistDetailResult;
import com.jachwisunbae.checklist.type.Stage;

public record UserChecklistDetailResponse(
        Long checklistId,
        String name,
        Stage stage,
        List<UserChecklistItemResponse> items) {

    public static UserChecklistDetailResponse from(UserChecklistDetailResult result) {
        return new UserChecklistDetailResponse(
                result.checklistId(), result.name(), result.stage(),
                result.items().stream().map(UserChecklistItemResponse::from).toList());
    }
}
