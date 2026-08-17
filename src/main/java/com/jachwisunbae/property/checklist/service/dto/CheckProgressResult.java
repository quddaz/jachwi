package com.jachwisunbae.property.checklist.service.dto;

import java.util.List;

import com.jachwisunbae.property.checklist.entity.AppliedChecklistItem;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public record CheckProgressResult(
        long totalCount,
        long completedCount,
        long goodCount,
        long cautionCount,
        long unconfirmedCount,
        int progressPercent) {

    public static CheckProgressResult from(List<AppliedChecklistItem> items) {
        long good = count(items, CheckStatus.GOOD);
        long caution = count(items, CheckStatus.CAUTION);
        long unconfirmed = count(items, CheckStatus.UNCONFIRMED);
        long total = items.size();
        long completed = good + caution;
        int percent = total == 0 ? 0 : (int) (completed * 100 / total);
        return new CheckProgressResult(total, completed, good, caution, unconfirmed, percent);
    }

    private static long count(List<AppliedChecklistItem> items, CheckStatus status) {
        long count = 0;
        for (AppliedChecklistItem item : items) {
            if (item.getStatus() == status) {
                count++;
            }
        }
        return count;
    }
}
