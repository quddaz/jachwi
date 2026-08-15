package com.jachwisunbae.checklist.entity;

import lombok.Getter;

@Getter
public class UserChecklistItem {

    private final Long id;
    private final Long userChecklistId;
    private final Long systemCheckItemId;
    private final int displayOrder;

    private UserChecklistItem(Long id, Long userChecklistId, Long systemCheckItemId, int displayOrder) {
        this.id = id;
        this.userChecklistId = userChecklistId;
        this.systemCheckItemId = systemCheckItemId;
        this.displayOrder = displayOrder;
    }

    public static UserChecklistItem restore(
            Long id,
            Long userChecklistId,
            Long systemCheckItemId,
            int displayOrder) {
        return new UserChecklistItem(id, userChecklistId, systemCheckItemId, displayOrder);
    }
}
