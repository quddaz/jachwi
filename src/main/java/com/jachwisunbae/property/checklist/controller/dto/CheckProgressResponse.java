package com.jachwisunbae.property.checklist.controller.dto;

import com.jachwisunbae.property.checklist.service.dto.CheckProgressResult;

public record CheckProgressResponse(
        long totalCount,
        long completedCount,
        long goodCount,
        long cautionCount,
        long unconfirmedCount,
        int progressPercent) {

    public static CheckProgressResponse from(CheckProgressResult result) {
        return new CheckProgressResponse(
                result.totalCount(), result.completedCount(), result.goodCount(),
                result.cautionCount(), result.unconfirmedCount(), result.progressPercent());
    }
}
