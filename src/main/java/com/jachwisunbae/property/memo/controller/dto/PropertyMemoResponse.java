package com.jachwisunbae.property.memo.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.jachwisunbae.property.memo.service.dto.PropertyMemoResult;

public record PropertyMemoResponse(List<ItemResponse> items, String freeMemo, LocalDateTime savedAt) {

    public static PropertyMemoResponse from(PropertyMemoResult result) {
        return new PropertyMemoResponse(
                result.items().stream().map(ItemResponse::from).toList(),
                result.freeMemo(),
                result.savedAt());
    }

    public record ItemResponse(String label, String content, int order) {
        private static ItemResponse from(PropertyMemoResult.Item item) {
            return new ItemResponse(item.label(), item.content(), item.order());
        }
    }
}
