package com.jachwisunbae.property.service.dto;

public record CheckProgressResult(long totalCount, long completedCount, int progressPercent) {

    public static CheckProgressResult of(long totalCount, long completedCount) {
        int percent = totalCount == 0 ? 0 : (int) (completedCount * 100 / totalCount);
        return new CheckProgressResult(totalCount, completedCount, percent);
    }
}
