package com.jachwisunbae.property.memo.controller.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import com.jachwisunbae.property.memo.service.dto.PropertyMemoResult;
import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemResult;

public record PropertyMemoResponse(
        List<PropertyMemoItemResponse> items,
        String freeMemo,
        LocalDateTime savedAt) {

    public static PropertyMemoResponse from(PropertyMemoResult result) {
        return new PropertyMemoResponse(
                toItemResponses(result),
                result.freeMemo(),
                result.savedAt());
    }

    private static List<PropertyMemoItemResponse> toItemResponses(PropertyMemoResult result) {
        List<PropertyMemoItemResponse> responses = new ArrayList<>();
        for (PropertyMemoItemResult item : result.items()) {
            responses.add(PropertyMemoItemResponse.from(item));
        }
        return List.copyOf(responses);
    }
}
