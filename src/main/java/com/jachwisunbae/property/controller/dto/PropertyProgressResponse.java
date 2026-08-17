package com.jachwisunbae.property.controller.dto;

import com.jachwisunbae.property.service.dto.CheckProgressResult;

public record PropertyProgressResponse(
        long totalCount,
        long completedCount,
        long goodCount,
        long cautionCount,
        long unconfirmedCount,
        int progressPercent) {

    public static PropertyProgressResponse from(CheckProgressResult result) {
        return new PropertyProgressResponse(
                result.totalCount(), result.completedCount(), result.goodCount(),
                result.cautionCount(), result.unconfirmedCount(), result.progressPercent());
    }
}
