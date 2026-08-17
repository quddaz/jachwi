package com.jachwisunbae.property.checklist.service.dto;

import java.util.List;

import com.jachwisunbae.property.checklist.entity.PropertyChecklistItem;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public record CheckProgressResult(
        long totalCount,
        long completedCount,
        long goodCount,
        long cautionCount,
        long unconfirmedCount,
        int progressPercent) {

    public static CheckProgressResult from(List<PropertyChecklistItem> items) {
        long good = count(items, CheckStatus.GOOD);
        long caution = count(items, CheckStatus.CAUTION);
        long unconfirmed = count(items, CheckStatus.UNCONFIRMED);
        long total = items.size();
        long completed = good + caution;
        int percent = total == 0 ? 0 : (int) (completed * 100 / total);
        return new CheckProgressResult(total, completed, good, caution, unconfirmed, percent);
    }

    private static long count(List<PropertyChecklistItem> items, CheckStatus status) {
        return items.stream().filter(item -> item.getStatus() == status).count();
    }
}
