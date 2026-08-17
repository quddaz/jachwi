package com.jachwisunbae.property.checklist.controller.dto;

import java.util.List;
import java.util.ArrayList;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.service.dto.AppliedChecklistDetailResult;
import com.jachwisunbae.property.checklist.service.dto.AppliedChecklistItemResult;

public record PropertyChecklistDetailResponse(
        Long propertyChecklistId,
        Long sourceChecklistId,
        String name,
        Stage stage,
        List<PropertyChecklistItemResponse> items,
        CheckProgressResponse progress) {

    public static PropertyChecklistDetailResponse from(AppliedChecklistDetailResult result) {
        return new PropertyChecklistDetailResponse(
                result.propertyChecklistId(), result.sourceChecklistId(), result.name(),
                result.stage(), toItemResponses(result),
                CheckProgressResponse.from(result.progress()));
    }

    private static List<PropertyChecklistItemResponse> toItemResponses(
            AppliedChecklistDetailResult result) {
        List<PropertyChecklistItemResponse> responses = new ArrayList<>();
        for (AppliedChecklistItemResult item : result.items()) {
            responses.add(PropertyChecklistItemResponse.from(item));
        }
        return List.copyOf(responses);
    }
}
