package com.jachwisunbae.property.service.dto;

public record CheckProgressResult(
        long totalCount,
        long completedCount,
        long goodCount,
        long cautionCount,
        long unconfirmedCount,
        int progressPercent) {

    public static CheckProgressResult of(long totalCount, long completedCount) {
        return of(totalCount, completedCount, 0, 0, totalCount - completedCount);
    }

    public static CheckProgressResult of(
            long totalCount,
            long completedCount,
            long goodCount,
            long cautionCount,
            long unconfirmedCount) {
        int percent = totalCount == 0 ? 0 : (int) (completedCount * 100 / totalCount);
        return new CheckProgressResult(
                totalCount, completedCount, goodCount, cautionCount, unconfirmedCount, percent);
    }
}
