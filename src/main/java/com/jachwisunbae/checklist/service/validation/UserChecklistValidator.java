package com.jachwisunbae.checklist.service.validation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

@Component
public class UserChecklistValidator {

    private static final int MAX_ITEM_COUNT = 100;

    public List<Long> validateItemIds(List<Long> ids, boolean allowEmpty) {
        if (ids == null
                || (!allowEmpty && ids.isEmpty())
                || ids.size() > MAX_ITEM_COUNT
                || ids.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEMS_INVALID,
                    "항목은 1개 이상 100개 이하여야 합니다.");
        }

        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(ids);
        if (distinctIds.size() != ids.size()) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEMS_INVALID,
                    "같은 항목을 중복할 수 없습니다.");
        }
        return List.copyOf(distinctIds);
    }

    public void validateExistingItems(List<Long> ids, List<SystemCheckItem> items) {
        if (items.size() != ids.size()) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEM_NOT_FOUND,
                    "시스템 체크 항목을 찾을 수 없습니다.");
        }
    }

    public void validateItems(
            Stage stage,
            List<SystemCheckItem> items,
            Set<Long> existingIds) {
        for (SystemCheckItem item : items) {
            validateSameStage(stage, item);
            validateActiveOrExisting(existingIds, item);
        }
    }

    public void validateFinalItemCount(List<Long> itemIds) {
        if (itemIds.isEmpty() || itemIds.size() > MAX_ITEM_COUNT) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEMS_INVALID,
                    "최종 항목은 1개 이상 100개 이하여야 합니다.");
        }
    }

    private void validateSameStage(Stage stage, SystemCheckItem item) {
        if (item.getStage() != stage) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEM_STAGE_MISMATCH,
                    "다른 단계의 항목을 추가할 수 없습니다.");
        }
    }

    private void validateActiveOrExisting(Set<Long> existingIds, SystemCheckItem item) {
        if (!item.isActive() && !existingIds.contains(item.getId())) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_INACTIVE_ITEM_NOT_ALLOWED,
                    "비활성 항목을 새로 추가할 수 없습니다.");
        }
    }
}
