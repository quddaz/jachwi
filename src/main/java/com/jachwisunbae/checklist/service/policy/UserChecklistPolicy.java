package com.jachwisunbae.checklist.service.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

@Component
public class UserChecklistPolicy {

    public List<Long> resolveCreateItemIds(
            List<Long> requestedIds,
            List<SystemCheckItem> activeCoreItems) {
        List<Long> finalIds = new ArrayList<>();
        activeCoreItems.stream().map(SystemCheckItem::getId).forEach(finalIds::add);
        requestedIds.stream().filter(id -> !finalIds.contains(id)).forEach(finalIds::add);
        return finalIds;
    }

    public void validateRequiredCoreItems(
            List<SystemCheckItem> existingItems,
            List<SystemCheckItem> activeCoreItems,
            List<Long> requestedIds) {
        Set<Long> requiredCoreIds = existingItems.stream()
                .filter(item -> item.getItemType() == ItemType.CORE)
                .map(SystemCheckItem::getId)
                .collect(Collectors.toSet());
        activeCoreItems.stream().map(SystemCheckItem::getId).forEach(requiredCoreIds::add);

        if (!requestedIds.containsAll(requiredCoreIds)) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_CORE_ITEM_REQUIRED,
                    "핵심 항목은 제거할 수 없습니다.");
        }
    }
}
