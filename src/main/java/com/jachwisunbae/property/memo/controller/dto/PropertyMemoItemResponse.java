package com.jachwisunbae.property.memo.controller.dto;

import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemResult;

public record PropertyMemoItemResponse(String label, String content, int order) {

    public static PropertyMemoItemResponse from(PropertyMemoItemResult result) {
        return new PropertyMemoItemResponse(result.label(), result.content(), result.order());
    }
}
